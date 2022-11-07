package llb.tdd.di;

import java.util.List;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: SingletonProvider
 * @date 2022-11-07 8:17:26
 * @ProjectName 01-di-container
 * @Version V1.0
 */
class SingletonProvider<T> implements ComponentProvider<T> {
	private T singleton;
	private ComponentProvider<T> provider;

	public SingletonProvider(ComponentProvider<T> provider) {
		this.provider = provider;
	}

	@Override
	public T get(Context context) {
		if (singleton == null) {
			singleton = provider.get(context);
		}
		return singleton;
	}

	@Override
	public List<ComponentRef<?>> getDependencies() {
		return provider.getDependencies();
	}
}
