package llb.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

	private Dependency dependency = mock(Dependency.class);
	private Context context = mock(Context.class);

	@BeforeEach
	public void setUp() {
		when(context.get(eq(Dependency.class))).thenReturn(Optional.of(dependency));
	}

	@Nested
	public class ConstructorInjection {

		@Nested
		public class Injection {
			static class DefaultConstructor {

			}

			@Test
			public void should_call_default_constructor_if_no_inject_constructor() {
				DefaultConstructor instance = new ConstructorInjectionProvider<>(DefaultConstructor.class).get(context);
				assertNotNull(instance);
			}

			static class InjectConstructor {
				Dependency dependency;
				@Inject
				public InjectConstructor(Dependency dependency) {
					this.dependency = dependency;
				}
			}

			@Test
			public void should_inject_dependency_via_inject_constructor() {
				InjectConstructor instance = new ConstructorInjectionProvider<>(InjectConstructor.class).get(context);
				assertNotNull(instance);
			}

			@Test
			public void should_include_dependency_from_inject_constructor() {
				ConstructorInjectionProvider<ComponentWithInjectConstructor> provider = new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class);
				assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
			}
		}

		@Nested
		class IllegalInjectConstructors {
			abstract class AbstractComponent implements Component {
				@Inject
				public AbstractComponent() {
				}
			}

			@Test
			public void should_throw_exception_if_component_is_abstract() {
				assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(AbstractComponent.class));
			}

			@Test
			public void should_throw_exception_if_component_is_interface() {
				assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class));
			}

			static class MultiInjectConstructors {
				@Inject
				public MultiInjectConstructors(AnotherDependency dependency) {
				}

				@Inject
				public MultiInjectConstructors(Dependency dependency) {
				}
			}

			@Test
			public void should_throw_exception_if_multi_inject_constructors_provided() {
				assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(MultiInjectConstructors.class));
			}

			static class NoInjectNorDefaultConstructor {
				public NoInjectNorDefaultConstructor(Dependency dependency) {
				}
			}

			@Test
			public void should_throw_exception_if_no_inject_constructors_provided() {
				assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(NoInjectNorDefaultConstructor.class));
			}
		}

	}

	@Nested
	public class FieldInjection {
		@Nested
		public class Injection {

			static class ComponentWithFiledInjection {
				@Inject
				Dependency dependency;
			}

			static class SubclassWithFiledInjection extends ComponentWithFiledInjection {
			}

			@Test
			public void should_inject_dependency_via_filed() {
				ComponentWithFiledInjection component = new ConstructorInjectionProvider<>(ComponentWithFiledInjection.class).get(context);

				assertSame(dependency, component.dependency);
			}

			@Test
			public void should_inject_dependency_via_superclass_inject_filed() {
				SubclassWithFiledInjection component = new ConstructorInjectionProvider<>(SubclassWithFiledInjection.class).get(context);

				assertSame(dependency, component.dependency);
			}

			@Test
			public void should_include_dependency_from_filed_dependency() {
				ConstructorInjectionProvider<ComponentWithFiledInjection> provider = new ConstructorInjectionProvider<>(ComponentWithFiledInjection.class);
				assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
			}
		}

		@Nested
		class IllegalInjectFields {
			static class FinalInjectField {
				@Inject
				final Dependency dependency = null;
			}

			@Test
			public void should_throw_exception_if_inject_field_is_final() {
				assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(FinalInjectField.class));
			}
		}
	}

	@Nested
	public class MethodInjection {

		@Nested
		public class Injection {
			static class InjectMethodWithNoDependency {
				boolean called = false;

				@Inject
				void install() {
					called = true;
				}
			}

			@Test
			public void should_call_inject_method_even_if_no_dependency_declared() {
				InjectMethodWithNoDependency component =
						new ConstructorInjectionProvider<>(InjectMethodWithNoDependency.class).get(context);
				assertTrue(component.called);
			}

			static class InjectMethodWithDependency {
				Dependency dependency;

				@Inject
				public InjectMethodWithDependency(Dependency dependency) {
					this.dependency = dependency;
				}
			}

			static class SuperClassWithInInjectMethod {
				int superCalled = 0;

				@Inject
				void install() {
					superCalled++;
				}
			}

			static class SubclassWithInInjectMethod extends SuperClassWithInInjectMethod {
				int subCalled = 0;

				@Inject
				void installAnother() {
					subCalled = superCalled + 1;
				}
			}

			@Test
			public void should_inject_dependency_via_inject_method() {
				InjectMethodWithDependency component = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class).get(context);
				assertSame(dependency, component.dependency);
			}

			static class SubclassOverrideSuperClassWithInInjectMethod extends SuperClassWithInInjectMethod {
				@Inject
				void install() {
					super.install();
				}
			}

			@Test
			public void should_inject_dependencies_via_inject_method_from_superclass() {

				SubclassWithInInjectMethod component = new ConstructorInjectionProvider<>(SubclassWithInInjectMethod.class).get(context);
				assertEquals(1, component.superCalled);
				assertEquals(2, component.subCalled);
			}

			static class SubclassOverrideSuperClassWithNoInject extends SuperClassWithInInjectMethod {
				void install() {
					super.install();
				}
			}

			@Test
			public void should_not_call_inject_method_if_override_with_no_inject() {

				SubclassOverrideSuperClassWithNoInject component = new ConstructorInjectionProvider<>(SubclassOverrideSuperClassWithNoInject.class).get(context);
				assertEquals(0, component.superCalled);
			}

			@Test
			public void should_include_dependencies_from_inject_method() {
				ConstructorInjectionProvider<InjectMethodWithDependency> provider = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class);
				assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
			}

			@Test
			public void should_only_call_once_if_subclass_override_inject_method_with_inject() {

				SubclassOverrideSuperClassWithInInjectMethod component = new ConstructorInjectionProvider<>(SubclassOverrideSuperClassWithInInjectMethod.class).get(context);
				assertEquals(1, component.superCalled);
			}
		}

		@Nested
		public class IllegalInjectionMethods {
			static class InjectMethodWithTypeParameter {
				@Inject
				<T> void install() {
				}
			}

			@Test
			public void should_throw_exception_if_inject_method_has_type_parameter() {
				assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(InjectMethodWithTypeParameter.class));
			}
		}
	}
}
