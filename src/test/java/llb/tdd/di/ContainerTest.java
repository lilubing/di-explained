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

interface Component {
	default Dependency dependency() {
		return null;
	}
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