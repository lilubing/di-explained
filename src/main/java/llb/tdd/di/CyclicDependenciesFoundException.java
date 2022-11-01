package llb.tdd.di;

import java.util.HashSet;
import java.util.Set;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: CyclicDependenciesFound
 * @date 2022-11-01 上午6:06
 * @ProjectName 01-di-container
 * @Version V1.0
 */
public class CyclicDependenciesFoundException extends RuntimeException {
    private Set<Class<?>> components = new HashSet<>();

	public CyclicDependenciesFoundException(Class<?> components) {
		this.components.add(components);
	}

	public CyclicDependenciesFoundException(Class<?> components, CyclicDependenciesFoundException e) {
		this.components.add(components);
		this.components.addAll(e.components);
	}

	public Class<?>[] getComponents() {
		return components.toArray(Class<?>[]::new);
	}
}
