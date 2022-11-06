package llb.tdd.di;

import java.util.Optional;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: Context
 * @date 2022-11-01 下午7:04
 * @ProjectName 01-di-container
 * @Version V1.0
 */
public interface Context {
    <ComponentType> Optional<ComponentType> get(ComponentRef<ComponentType> ref);

}
