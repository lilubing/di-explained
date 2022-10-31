package llb.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: Context
 * @date 2022-10-31 7:41
 * @ProjectName 01-di-container
 * @Version V1.0
 */
public class Context {

	private Map<Class<?>, Provider<?>> providers = new HashMap<>();

	public <Type> void bind(Class<Type> type, Type instance) {
		providers.put(type, (Provider<Type>) () -> instance);
	}

	public <Type, Implementation extends Type>
	void bind(Class<Type> type, Class<Implementation> implementation) {
		Constructor<?>[] injectConstructors = stream(implementation.getConstructors()).filter(c -> c.isAnnotationPresent(Inject.class))
				.toArray(Constructor<?>[]::new);
		/*if (injectConstructors.length > 1) throw new IllegalComponentException();
		if (injectConstructors.length == 0 && stream(implementation.getConstructors())
				.filter(c -> c.getParameters().length == 0).findFirst().map(c -> false).orElse(true))
			throw new IllegalComponentException();*/
		providers.put(type, (Provider<Type>) () -> {
			try {
				Constructor<Implementation> injectConstructor = getInjectConstructor(implementation);
				Object[] dependencies = stream(injectConstructor.getParameters())
						.map(p -> get(p.getType()))
						.toArray(Object[]::new);
				return injectConstructor.newInstance(dependencies);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
		return (Constructor<Type>) stream(implementation.getConstructors())
				.filter(c -> c.isAnnotationPresent(Inject.class)).findFirst().orElseGet(() -> {
					try {
						return implementation.getConstructor();
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e);
					}
				});
	}

	public <Type> Type get(Class<Type> type) {
		return (Type) providers.get(type).get();
	}
}