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

				DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext().get(Component.class));

				assertEquals(String.class, exception.getDependency());
				assertEquals(Dependency.class, exception.getComponent());
			}

			@Test
			public void should_throw_exception_if_cyclic_dependency_found() {
				config.bind(Component.class, ComponentWithInjectConstructor.class);
				config.bind(Dependency.class, DependencyDependedOnComponent.class);

				CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext().get(Component.class));

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

				CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext().get(Component.class));
				List<Class<?>> classes = Arrays.asList(exception.getComponents());

				assertEquals(3, classes.size());
				assertTrue(classes.contains(Component.class));
				assertTrue(classes.contains(Dependency.class));
				assertTrue(classes.contains(AnotherDependency.class));
			}

			@Nested
			public class FieldInjection {
			}

			@Nested
			public class MethodInjection {
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