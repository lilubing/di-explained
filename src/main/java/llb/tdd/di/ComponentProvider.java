package llb.tdd.di;

import java.util.List;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ComponentProvider
 * @date 2022-11-07 8:18:02
 * @ProjectName 01-di-container
 * @Version V1.0
 */
interface ComponentProvider<T> {
	T get(Context context);

	default List<ComponentRef<?>> getDependencies() {
		return List.of();
	}
}
