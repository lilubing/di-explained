package llb.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: InjectionTest
 * @date 2022-11-02 8:16
 * @ProjectName 01-di-container
 * @Version V1.0
 */
@Nested
public class InjectionTest {

	ContextConfig config;

	@BeforeEach
	public void setUp() {
		config = new ContextConfig();
	}

	@Nested
	public class ConstructorInjection {
		@Test
		public void should_bind_type_to_a_class_with_default_constructor() {
			config.bind(Component.class, ComponentWithDefaultConstructor.class);
			Component instance = config.getContext().get(Component.class).get();
			assertNotNull(instance);
			assertTrue(instance instanceof ComponentWithDefaultConstructor);
		}

		@Test
		public void should_bind_type_to_a_class_with_inject_constructor() {
			Dependency dependency = new Dependency() {
			};
			config.bind(Component.class, ComponentWithInjectConstructor.class);
			config.bind(Dependency.class, dependency);

			Component instance = config.getContext().get(Component.class).get();
			assertNotNull(instance);
			assertSame(dependency, ((ComponentWithInjectConstructor) instance).getDependency());
		}

		@Test
		public void should_bind_type_to_a_class_with_transitive_dependencies() {
			config.bind(Component.class, ComponentWithInjectConstructor.class);
			config.bind(Dependency.class, DependencyWithInjectConstructor.class);
			config.bind(String.class, "indirect dependency");

			Component instance = config.getContext().get(Component.class).get();
			assertNotNull(instance);

			Dependency dependency = ((ComponentWithInjectConstructor) instance).getDependency();
			assertNotNull(dependency);

			assertEquals("indirect dependency", ((DependencyWithInjectConstructor) dependency).getDependency());

		}

		abstract class AbstractComponent implements Component {
			@Inject
			public AbstractComponent() {

			}
		}

		@Test
		public void should_throw_exception_if_component_is_abstract() {
			assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(ConstructorInjection.AbstractComponent.class));
		}

		@Test
		public void should_throw_exception_if_component_is_interface() {
			assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class));
		}

		@Test
		public void should_throw_exception_if_multi_inject_constructors_provided() {
			assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(ComponentWithMultiInjectConstructors.class));
		}

		@Test
		public void should_throw_exception_if_no_inject_constructors_provided() {
			assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(ComponentWithNoInjectConstructorNorDefaultConstructor.class));
		}

		@Test
		public void should_include_dependency_from_inject_constructor() {
			ConstructorInjectionProvider<ComponentWithInjectConstructor> provider = new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class);
			assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
		}

	}

	@Nested
	public class FieldInjection {
		static class ComponentWithFiledInjection {
			@Inject
			Dependency dependency;
		}

		static class SubclassWithFiledInjection extends FieldInjection.ComponentWithFiledInjection {
		}

		@Test
		public void should_inject_dependency_via_filed() {
			Dependency dependency = new Dependency() {
			};
			config.bind(Dependency.class, dependency);
			config.bind(FieldInjection.ComponentWithFiledInjection.class, FieldInjection.ComponentWithFiledInjection.class);

			FieldInjection.ComponentWithFiledInjection component = config.getContext().get(FieldInjection.ComponentWithFiledInjection.class).get();

			assertSame(dependency, component.dependency);
		}

		@Test
		public void should_inject_dependency_via_superclass_inject_filed() {
			Dependency dependency = new Dependency() {
			};
			config.bind(Dependency.class, dependency);
			config.bind(FieldInjection.SubclassWithFiledInjection.class, FieldInjection.SubclassWithFiledInjection.class);

			FieldInjection.SubclassWithFiledInjection component = config.getContext().get(FieldInjection.SubclassWithFiledInjection.class).get();

			assertSame(dependency, component.dependency);
		}

		@Test
		public void should_include_filed_dependency_in_dependencies() {
			ConstructorInjectionProvider<FieldInjection.ComponentWithFiledInjection> provider = new ConstructorInjectionProvider<>(FieldInjection.ComponentWithFiledInjection.class);
			assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
		}

		static class InjectMethodWithDependency {
			Dependency dependency;

			@Inject
			public InjectMethodWithDependency(Dependency dependency) {
				this.dependency = dependency;
			}
		}

		@Test
		public void should_inject_dependency_via_inject_method() {
			Dependency dependency = new Dependency() {
			};
			config.bind(Dependency.class, dependency);
			config.bind(FieldInjection.InjectMethodWithDependency.class, FieldInjection.InjectMethodWithDependency.class);

			FieldInjection.InjectMethodWithDependency component = config.getContext().get(FieldInjection.InjectMethodWithDependency.class).get();
			assertSame(dependency, component.dependency);
		}

		static class SuperClassWithInInjectMethod {
			int superCalled = 0;

			@Inject
			void install() {
				superCalled++;
			}
		}

		static class SubclassWithInInjectMethod extends FieldInjection.SuperClassWithInInjectMethod {
			int subCalled = 0;

			@Inject
			void installAnother() {
				subCalled = superCalled + 1;
			}
		}

		static class SubclassOverrideSuperClassWithInInjectMethod extends FieldInjection.SuperClassWithInInjectMethod {
			@Inject
			void install() {
				super.install();
			}
		}

		@Test
		public void should_only_call_once_if_subclass_override_inject_method_with_inject() {
			config.bind(FieldInjection.SubclassOverrideSuperClassWithInInjectMethod.class, FieldInjection.SubclassOverrideSuperClassWithInInjectMethod.class);
			FieldInjection.SubclassOverrideSuperClassWithInInjectMethod component = config.getContext().get(FieldInjection.SubclassOverrideSuperClassWithInInjectMethod.class).get();
			assertEquals(1, component.superCalled);
		}

		static class SubclassOverrideSuperClassWithNoInject extends FieldInjection.SuperClassWithInInjectMethod {
			void install() {
				super.install();
			}
		}

		@Test
		public void should_not_call_inject_method_if_override_with_no_inject() {
			config.bind(FieldInjection.SubclassOverrideSuperClassWithNoInject.class, FieldInjection.SubclassOverrideSuperClassWithNoInject.class);
			FieldInjection.SubclassOverrideSuperClassWithNoInject component = config.getContext().get(FieldInjection.SubclassOverrideSuperClassWithNoInject.class).get();
			assertEquals(0, component.superCalled);
		}

		@Test
		public void should_inject_dependencies_via_inject_method_from_superclass() {
			config.bind(FieldInjection.SubclassWithInInjectMethod.class, FieldInjection.SubclassWithInInjectMethod.class);
			FieldInjection.SubclassWithInInjectMethod component = config.getContext().get(FieldInjection.SubclassWithInInjectMethod.class).get();
			assertEquals(1, component.superCalled);
			assertEquals(2, component.subCalled);
		}

		@Test
		public void should_include_dependencies_from_inject_method() {
			ConstructorInjectionProvider<FieldInjection.InjectMethodWithDependency> provider = new ConstructorInjectionProvider<>(FieldInjection.InjectMethodWithDependency.class);
			assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
		}

		static class FinalInjectField {
			@Inject
			final Dependency dependency = null;
		}

		@Test
		public void should_throw_exception_if_inject_field_is_final() {
			assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(FieldInjection.FinalInjectField.class));
		}


	}

	@Nested
	public class MethodInjection {
		static class InjectMethodWithNoDependency {
			boolean called = false;

			@Inject
			void install() {
				called = true;
			}
		}

		@Test
		public void should_call_inject_method_even_if_no_dependency_declared() {
			config.bind(MethodInjection.InjectMethodWithNoDependency.class, MethodInjection.InjectMethodWithNoDependency.class);
			MethodInjection.InjectMethodWithNoDependency component = config.getContext().get(MethodInjection.InjectMethodWithNoDependency.class).get();
			assertTrue(component.called);
		}

		static class InjectMethodWithTypeParameter {
			@Inject
			<T> void install() {
			}
		}

		@Test
		public void should_throw_exception_if_inject_method_has_type_parameter() {
			assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(MethodInjection.InjectMethodWithTypeParameter.class));
		}
	}
}
