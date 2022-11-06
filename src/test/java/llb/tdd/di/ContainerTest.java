package llb.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

public class ContainerTest {

	ContextConfig config;

	@BeforeEach
	public void setUp() {
		config = new ContextConfig();
	}

	@Nested
	public class DependenciesSelection {
		@Nested
		public class ProviderType {

		}
		@Nested
		public class Qualifier {
		}
	}

	@Nested
	public class LifecycleManagement {
	}


}

interface TestComponent {
	default Dependency dependency() {
		return null;
	}
}

interface Dependency {

}

interface AnotherDependency {

}

class ComponentWithDefaultConstructor implements TestComponent {
	public ComponentWithDefaultConstructor() {

	}
}

class ComponentWithInjectConstructor implements TestComponent {
	private Dependency dependency;

	@Inject
	public ComponentWithInjectConstructor(Dependency dependency) {
		this.dependency = dependency;
	}

	public Dependency getDependency() {
		return dependency;
	}
}

class ComponentWithMultiInjectConstructors implements TestComponent {
	@Inject
	public ComponentWithMultiInjectConstructors(String name, Double value) {

	}

	@Inject
	public ComponentWithMultiInjectConstructors(String name) {

	}
}

class ComponentWithNoInjectConstructorNorDefaultConstructor implements TestComponent {
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
	private TestComponent component;

	@Inject
	public DependencyDependedOnComponent(TestComponent component) {
		this.component = component;
	}
}

class AnotherDependencyDependedOnComponent implements AnotherDependency {
	private TestComponent component;

	@Inject
	public AnotherDependencyDependedOnComponent(TestComponent component) {
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