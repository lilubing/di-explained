package llb.tdd.di;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: DependencyNotFoundException
 * @date 2022-10-31 下午8:45
 * @ProjectName 01-di-container
 * @Version V1.0
 */
public class DependencyNotFoundException extends RuntimeException {

	private Component component;
	private Component dependency;

	public DependencyNotFoundException(Component component, Component dependency) {
		this.component = component;
		this.dependency = dependency;
	}

	public Component getDependency() {
		return dependency;
	}

	public Component getComponent() {
		return component;
	}
}
