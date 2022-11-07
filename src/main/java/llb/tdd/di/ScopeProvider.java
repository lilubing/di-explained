package llb.tdd.di;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ScopeProvider
 * @date 2022-11-07 8:23:33
 * @ProjectName 01-di-container
 * @Version V1.0
 */
interface ScopeProvider {
	ComponentProvider<?> create(ComponentProvider<?> provider);
}
