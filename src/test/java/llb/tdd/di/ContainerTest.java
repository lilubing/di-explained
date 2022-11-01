package llb.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContainerTest {

	ContextConfig config;

	@BeforeEach
	public void setUp() {
		config = new ContextConfig();
	}


	@Nested
	public class ComponentConstruction {
		@Test
		public void should_bind_type_to_a_specific_instance() {
			Component instance = new Component() {
			};
			config.bind(Component.class, instance);

			Context context = config.getContext();
			assertSame(instance, context.get(Component.class).get());
		}

		// TODO: abstract class
		// TODO: interface
		@Test
		public void should_return_empty_if_component_not_defined() {
//			assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class));
			Optional<Component> component = config.getContext().get(Component.class);
			assertTrue(component.isEmpty());
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

			@Test
			public void should_throw_exception_if_multi_inject_constructors_provided() {
				assertThrows(IllegalComponentException.class, () ->{
					config.bind(Component.class, ComponentWithMultiInjectConstructors.class);
				});
			}

			@Test
			public void should_throw_exception_if_no_inject_constructors_provided() {
				assertThrows(IllegalComponentException.class, () ->{
					config.bind(Component.class, ComponentWithNoInjectConstructorNorDefaultConstructor.class);
				});

				/*context.bind(Component.class, ComponentWithNoInjectConstructorNorDefaultConstructor.class);
				assertThrows(IllegalComponentException.class, () ->{
					context.get(Component.class);
				});*/
			}

			@Test
			public void should_throw_exception_if_dependency_not_found() {
				config.bind(Component.class, ComponentWithInjectConstructor.class);
				DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
				assertEquals(Dependency.class, exception.getDependency());
				assertEquals(Component.class, exception.getComponent());
			}

			@Test
			public void should_throw_exception_if_transitive_dependency_not_found() {
				config.bind(Component.class, ComponentWithInjectConstructor.class);
				config.bind(Dependency.class, DependencyWithInjectConstructor.class);

				DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

				assertEquals(String.class, exception.getDependency());
				assertEquals(Dependency.class, exception.getComponent());
			}

			@Test
			public void should_throw_exception_if_cyclic_dependency_found() {
				config.bind(Component.class, ComponentWithInjectConstructor.class);
				config.bind(Dependency.class, DependencyDependedOnComponent.class);

				CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

				Set<Class<?>> classes = Sets.newSet(exception.getComponents());
				assertEquals(2, classes.size());
				assertTrue(classes.contains(Component.class));
				assertTrue(classes.contains(Dependency.class));

			}

			@Test
			public void should_throw_exception_if_transitive_cyclic_dependency_found() {
				config.bind(Component.class, ComponentWithInjectConstructor.class);
				config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
				config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

				CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());
				List<Class<?>> classes = Arrays.asList(exception.getComponents());

				assertEquals(3, classes.size());
				assertTrue(classes.contains(Component.class));
				assertTrue(classes.contains(Dependency.class));
				assertTrue(classes.contains(AnotherDependency.class));
			}

			@Nested
			public class FieldInjection {
				static class ComponentWithFiledInjection {
					@Inject
					Dependency dependency;
				}

				static class SubclassWithFiledInjection extends ComponentWithFiledInjection {
				}

				@Test
				public void should_inject_dependency_via_filed() {
					Dependency dependency = new Dependency() {
					};
					config.bind(Dependency.class, dependency);
					config.bind(ComponentWithFiledInjection.class, ComponentWithFiledInjection.class);

					ComponentWithFiledInjection component = config.getContext().get(ComponentWithFiledInjection.class).get();

					assertSame(dependency, component.dependency);
				}

				@Test
				public void should_inject_dependency_via_superclass_inject_filed() {
					Dependency dependency = new Dependency() {
					};
					config.bind(Dependency.class, dependency);
					config.bind(SubclassWithFiledInjection.class, SubclassWithFiledInjection.class);

					SubclassWithFiledInjection component = config.getContext().get(SubclassWithFiledInjection.class).get();

					assertSame(dependency, component.dependency);
				}

				@Test
				public void should_include_filed_dependency_in_dependencies() {
					ConstructorInjectionProvider<ComponentWithFiledInjection> provider = new ConstructorInjectionProvider<>(ComponentWithFiledInjection.class);
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
					config.bind(InjectMethodWithDependency.class, InjectMethodWithDependency.class);

					InjectMethodWithDependency component = config.getContext().get(InjectMethodWithDependency.class).get();
					assertSame(dependency, component.dependency);
				}

				static class SuperClassWithInInjectMethod {
					int superCalled = 0;
					@Inject
					void install() {
						superCalled ++;
					}
				}

				static class SubclassWithInInjectMethod extends SuperClassWithInInjectMethod {
					int subCalled = 0;
					@Inject
					void installAnother() {
						subCalled = superCalled + 1;
					}
				}

				static class SubclassOverrideSuperClassWithInInjectMethod extends SuperClassWithInInjectMethod {
					@Inject
					void install() {
						super.install();
					}
				}

				@Test
				public void should_only_call_once_if_subclass_override_inject_method_with_inject() {
					config.bind(SubclassOverrideSuperClassWithInInjectMethod.class, SubclassOverrideSuperClassWithInInjectMethod.class);
					SubclassOverrideSuperClassWithInInjectMethod component = config.getContext().get(SubclassOverrideSuperClassWithInInjectMethod.class).get();
					assertEquals(1, component.superCalled);
				}

				static class SubclassOverrideSuperClassWithNoInject extends SuperClassWithInInjectMethod {
					void install() {
						super.install();
					}
				}

				@Test
				public void should_not_call_inject_method_if_override_with_no_inject() {
					config.bind(SubclassOverrideSuperClassWithNoInject.class, SubclassOverrideSuperClassWithNoInject.class);
					SubclassOverrideSuperClassWithNoInject component = config.getContext().get(SubclassOverrideSuperClassWithNoInject.class).get();
					assertEquals(0, component.superCalled);
				}

				@Test
				public void should_inject_dependencies_via_inject_method_from_superclass() {
					config.bind(SubclassWithInInjectMethod.class, SubclassWithInInjectMethod.class);
					SubclassWithInInjectMethod component = config.getContext().get(SubclassWithInInjectMethod.class).get();
					assertEquals(1, component.superCalled);
					assertEquals(2, component.subCalled);
				}

				@Test
				public void should_include_dependencies_from_inject_method() {
					ConstructorInjectionProvider<InjectMethodWithDependency> provider = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class);
					assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
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
					config.bind(InjectMethodWithNoDependency.class, InjectMethodWithNoDependency.class);
					InjectMethodWithNoDependency component = config.getContext().get(InjectMethodWithNoDependency.class).get();
					assertTrue(component.called);
				}
			}

		}

		@Nested
		public class DependenciesSelection {
		}

		@Nested
		public class LifecycleManagement {
		}

	}

}

interface Component {
}

interface Dependency {

}

interface AnotherDependency {

}

class ComponentWithDefaultConstructor implements Component {
	public ComponentWithDefaultConstructor() {

	}
}

class ComponentWithInjectConstructor implements Component {
	private Dependency dependency;

	@Inject
	public ComponentWithInjectConstructor(Dependency dependency) {
		this.dependency = dependency;
	}

	public Dependency getDependency() {
		return dependency;
	}
}

class ComponentWithMultiInjectConstructors implements Component {
	@Inject
	public ComponentWithMultiInjectConstructors(String name, Double value) {

	}

	@Inject
	public ComponentWithMultiInjectConstructors(String name) {

	}
}

class ComponentWithNoInjectConstructorNorDefaultConstructor implements Component {
	public ComponentWithNoInjectConstructorNorDefaultConstructor(String name) {

	}
}

class DependencyWithInjectConstructor implements Dependency {
	private String dependency;

	@Inject
	public DependencyWithInjectConstructor(String dependency) {
		this.dependency = dependency;
	}

	public String getDependency() {
		return dependency;
	}
}

class DependencyDependedOnComponent implements Dependency {
	private Component component;
	@Inject
	public DependencyDependedOnComponent(Component component) {
		this.component = component;
	}
}

class AnotherDependencyDependedOnComponent implements AnotherDependency {
	private Component component;
	@Inject
	public AnotherDependencyDependedOnComponent(Component component) {
		this.component = component;
	}
}

class DependencyDependedOnAnotherDependency implements Dependency {
	private AnotherDependency anotherDependency;
	@Inject
	public DependencyDependedOnAnotherDependency(AnotherDependency anotherDependency) {
		this.anotherDependency = anotherDependency;
	}
}