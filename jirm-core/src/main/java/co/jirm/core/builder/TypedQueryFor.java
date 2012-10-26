package co.jirm.core.builder;

import java.util.List;

import com.google.common.base.Optional;


public interface TypedQueryFor<T> {
	List<T> forList();
	Optional<T> forOptional();
	T forObject();
}
