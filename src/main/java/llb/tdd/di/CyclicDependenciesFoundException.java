package llb.tdd.di;

import java.util.HashSet;
import java.util.List;
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
    private Set<Component> components = new HashSet<>();

	public CyclicDependenciesFoundException(List<Component> visiting) {
		components.addAll(visiting);
	}

    public Class<?>[] getComponents() {
		return components.stream().map(c -> c.type()).toArray(Class<?>[]::new);
	}
}
