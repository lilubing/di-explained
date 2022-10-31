package llb.tdd.di;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: CyclicDependenciesFound
 * @date 2022-11-01 上午6:06
 * @ProjectName 01-di-container
 * @Version V1.0
 */
public class CyclicDependenciesFound extends RuntimeException {
    public Class<?> getDependency() {
        return null;
    }
}
