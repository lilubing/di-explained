package llb.tdd.di;

import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.List.of;

public class ContextConfig {
    private Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, (ComponentProvider<Type>) context -> instance);
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        providers.put(type, new InjectionProvider(implementation));
    }

    public Context getContext() {
        providers.keySet().forEach(component -> checkDependencies(component, new Stack<>()));
        return new Context() {
            @Override
            public Optional get(Type type) {
                if (isaContainerType(type)) {
                    return getContainer((ParameterizedType) type);
                }
                return getComponent((Class<?>) type);
            }

            private <Type> Optional<Type> getComponent(Class<Type> type) {
                Type containerType = null;
                Class<Type> componentType = type;
                return Optional.ofNullable(providers.get(componentType)).map(provider -> (Type) provider.get(this));
            }

            private Optional getContainer(ParameterizedType type) {
                Type containerType = type.getRawType();
                Class<?> componentType = getComponentType(type);

                if(containerType != Provider.class) {
                    return Optional.empty();
                }

                return Optional.ofNullable(providers.get(componentType))
                        .map(provider -> (Provider<Object>) () -> provider.get(this));
            }
        };
    }

    static class Ref {

    }

    private Class<?> getComponentType(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private boolean isaContainerType(Type type) {
        return type instanceof ParameterizedType;
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Type dependency : providers.get(component).getDependencies()) {
            if(isaContainerType(dependency)) {
                checkContainerTypeDependency(component, getComponentType(dependency));
            } else {
                checkComponentDependency(component, visiting, (Class<?>) dependency);
            }
        }
    }

    private void checkContainerTypeDependency(Class<?> component, Class<?> componentType) {
        if (!providers.containsKey(componentType)) {
            throw new DependencyNotFoundException(component, componentType);
        }
    }

    private void checkComponentDependency(Class<?> component, Stack<Class<?>> visiting, Class<?> dependency) {
        checkContainerTypeDependency(component, dependency);
        if (visiting.contains(dependency)) {
            throw new CyclicDependenciesFoundException(visiting);
        }
        visiting.push(dependency);
        checkDependencies(dependency, visiting);
        visiting.pop();
    }

    interface ComponentProvider<T> {
        T get(Context context);

        default List<Type> getDependencies() {
            return of();
        }
    }

}