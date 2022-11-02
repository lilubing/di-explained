package llb.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.collections.Sets;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContainerTest {

	ContextConfig config;

	@BeforeEach
	public void setUp() {
		config = new ContextConfig();
	}

	@Nested
	class TypeBinding {
		@Test
		public void should_bind_type_to_a_specific_instance() {
			Component instance = new Component() {
			};
			config.bind(Component.class, instance);

			Context context = config.getContext();
			assertSame(instance, context.get(Component.class).get());
		}

		@ParameterizedTest(name = "supporting [0]")
		@MethodSource
		public void should_bind_type_to_an_injectable_component(Class<? extends Component> componentType) {
			Dependency dependency = new Dependency() {
			};
			config.bind(Dependency.class, dependency);
			config.bind(Component.class, componentType);

			Optional<Component> component = config.getContext().get(Component.class);

			assertTrue(component.isPresent());
	//		assertSame(dependency, component.get().dependency());
		}

		public static Stream<Arguments> should_bind_type_to_an_injectable_component() {

			return Stream.of(Arguments.of(Named.of("Constructor Injection", TypeBinding.ConstructorInjection.class),
					Arguments.of(Named.of("Field Injection", TypeBinding.FieldInjection.class)),
					Arguments.of(Named.of("Method Injection", TypeBinding.MethodInjection.class))));
		}

		static class ConstructorInjection implements Component {
			private Dependency dependency;
			@Inject
			public ConstructorInjection (Dependency dependency) {
				this.dependency = dependency;
			}
			@Override
			public Dependency dependency() {
				return dependency;
			}
		}

		static class FieldInjection implements Component {
			@Inject
			Dependency dependency;
			@Override
			public Dependency dependency() { return dependency;}
		}
		static class MethodInjection implements Component {
			private Dependency dependency;
			@Inject
			void install(Dependency dependency) { this.dependency = dependency; }
			@Override
			public Dependency dependency() { return dependency;}
		}

		@Test
		public void should_return_empty_if_component_not_defined() {
			Optional<Component> component = config.getContext().get(Component.class);
			assertTrue(component.isEmpty());
		}

		@Test
		public void should_retrieve_empty_for_unbind_type() {
			Optional<Component> component = config.getContext().get(Component.class);
			assertTrue(component.isEmpty());
		}
	}

	@Nested
	public class ComponentConstruction {

		@Nested
		public class DependencyCheck {

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
			public void should_throw_exception_if_dependency_not_found() {
				config.bind(Component.class, ComponentWithInjectConstructor.class);
				DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
				assertEquals(Dependency.class, exception.getDependency());
				assertEquals(Component.class, exception.getComponent());
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
		}

	}

}

interface Component {
	default Dependency dependency() {return null;}
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