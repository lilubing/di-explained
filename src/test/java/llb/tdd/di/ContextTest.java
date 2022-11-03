package llb.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.collections.Sets;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ContextTest
 * @date 2022-11-03 8:31:44
 * @ProjectName 01-di-container
 * @Version V1.0
 */
class ContextTest {
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
			public ConstructorInjection(Dependency dependency) {
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
			public Dependency dependency() {
				return dependency;
			}
		}

		static class MethodInjection implements Component {
			private Dependency dependency;

			@Inject
			void install(Dependency dependency) {
				this.dependency = dependency;
			}

			@Override
			public Dependency dependency() {
				return dependency;
			}
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

		// context
		// TOD0 could get Provider<T> from context
		@Test
		public void should_retrieve_bind_type_as_provider() {
			Component instance = new Component() {
			};
			config.bind(Component.class, instance);

			Context context = config.getContext();

			Type type = new TypeLiteral<Provider<Component>>() {}.getType();

			/*Provider<Component> provider = context.get(Provider<Component.class);
			assertSame(instance, provider.get());*/
		}

		static abstract class TypeLiteral<T> {
			public Type getType() {
				return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
			}
		}
	}

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

		@ParameterizedTest
		@MethodSource
		public void should_throw_exception_if_dependency_not_found(Class<? extends Component> component) {
			config.bind(Component.class, component);
			DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

			assertEquals(Dependency.class, exception.getDependency());
			assertEquals(Component.class, exception.getComponent());
		}

		public static Stream<Arguments> should_throw_exception_if_dependency_not_found() {
			return Stream.of(Arguments.of(Named.of("Inject Constructor", DependencyCheck.MissingDependencyConstructor.class),
					Arguments.of(Named.of("Inject Field", DependencyCheck.MissingDependencyField.class)),
					Arguments.of(Named.of("Inject Method", DependencyCheck.MissingDependencyMethod.class))));
		}

		static class MissingDependencyConstructor implements Component {
			@Inject
			public MissingDependencyConstructor(Dependency dependency) {
			}
		}

		static class MissingDependencyField implements Component {
			@Inject
			Dependency dependency;
		}

		static class MissingDependencyMethod implements Component {
			@Inject
			void install(Dependency dependency) {
			}
		}

		@ParameterizedTest(name = "cyclic dependency between {0} and {1}")
		@MethodSource
		public void should_throw_exception_if_cyclic_dependencies_found(Class<? extends Component> component,
																		Class<? extends Dependency> dependency) {
			config.bind(Component.class, component);
			config.bind(Dependency.class, dependency);

			CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

			Set<Class<?>> classes = Sets.newSet(exception.getComponents());

			assertEquals(2, classes.size());
			assertTrue(classes.contains(Component.class));
			assertTrue(classes.contains(Dependency.class));
		}

		public static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
			List<Arguments> arguments = new ArrayList<>();
			for (Named component : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicComponentInjectConstructor.class),
					Named.of("Inject Field", DependencyCheck.CyclicComponentInjectField.class),
					Named.of("Inject Method", DependencyCheck.CyclicComponentInjectMethod.class))) {
				for (Named dependency : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicDependencyInjectMethod.class),
						Named.of("Inject Field", DependencyCheck.CyclicDependencyInjectField.class),
						Named.of("Inject Method", DependencyCheck.CyclicDependencyInjectMethod.class))) {
					arguments.add(Arguments.of(component, dependency));
				}
			}
			return arguments.stream();
		}

		static class CyclicComponentInjectConstructor implements Component {
			@Inject
			public CyclicComponentInjectConstructor(Dependency dependency) {
			}
		}

		static class CyclicComponentInjectField implements Component {
			@Inject
			Dependency dependency;

		}

		static class CyclicComponentInjectMethod implements Component {
			@Inject
			void install(Dependency dependency) {
			}
		}

		static class CyclicDependencyInjectConstructor implements Dependency {
			@Inject
			public CyclicDependencyInjectConstructor(Component component) {
			}
		}

		static class CyclicDependencyInjectField implements Component {
			@Inject
			Component component;

		}

		static class CyclicDependencyInjectMethod implements Component {
			@Inject
			void install(Component component) {
			}
		}

			/*@ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
			@MethodSource
			public void should_throw_exception_if_transitive_cyclic_dependency_found(Class<? extends Component> component,
																					 Class<? extends Dependency> dependency,
																					 Class<? extends AnotherDependency> anotherDependency) {

				config.bind(Component.class, component);
				config.bind(Dependency.class, dependency);
				config.bind(AnotherDependency.class, anotherDependency);

				CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());
				List<Class<?>> components = Arrays.asList(exception.getComponents());

				assertEquals(3, components.size());
				assertTrue(components.contains(Component.class));
				assertTrue(components.contains(Dependency.class));
				assertTrue(components.contains(AnotherDependency.class));
			}
		}

		*public static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found() {
			List<Arguments> arguments = new ArrayList<>();
			for (Named component : List.of(Named.of( "Inject Constructor1",DependencyCheck.CyclicComponentInjectConstructor.class),
				Named.of( "Inject Field", DependencyCheck.CyclicComponentInjectField.class),
				Named.of(  "Inject Method",DependencyCheck.CyclicComponentInjectMethod.class))) {
				for (Named dependency : List.of(Named.of( "Inject Constructor",DependencyCheck.IndirectCyclicDependet),
					Named.of( "Inject Field",DependencyCheck.IndirectCyclicDependencyInjectField.class),
					Named.of( "Inject Method",DependencyCheck.IndirectCyclicDependencyInjectMethod.class))) {
					for (Named anotherDependency : List.of(Named.of( "Inject Constructor1",DependencyCheck.IndirectCyclicDependencyInjectConstructor
							Named.of( "Inject Field",DependencyCheck.IndirectCyclicAnotherDependencyInjectField.class),
							Named.of( "Inject Method",DependencyCheck.IndirectCyclicAnotherDependencyInjectMethod.class)) {
							arguments.add(Arguments.of(component,dependency, anotherDependency));
					}
				}
			}
			return arguments.stream();
		}
		static class IndirectCyclicDependencyInjectConstructor implements Dependency {
			@Inject
			public IndirectCyclicDependencyInjectConstructor(AnotherDependency anotherDependency) {
			}
		}*/

	}

}
