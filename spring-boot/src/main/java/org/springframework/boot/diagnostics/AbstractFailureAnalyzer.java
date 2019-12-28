
package org.springframework.boot.diagnostics;

import org.springframework.core.ResolvableType;

/**
 * Abstract base class for most {@code FailureAnalyzer} implementations.
 * 故障分析器实现的抽象基类。
 *
 * @param <T> the type of exception to analyze
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 1.4.0
 */
public abstract class AbstractFailureAnalyzer<T extends Throwable>
		implements FailureAnalyzer {

	@Override
	public FailureAnalysis analyze(Throwable failure) {
		// 查找根因异常
		T cause = findCause(failure, getCauseType());
		if (cause != null) {
			// 分析故障原因
			return analyze(failure, cause);
		}
		return null;
	}

	/**
	 * Returns an analysis of the given {@code failure}, or {@code null} if no analysis
	 * was possible.
	 * 返回对给定故障的分析。
	 * 如果无法进行分析，则返回null。
	 * @param rootFailure the root failure passed to the analyzer 根故障异常
	 * @param cause the actual found cause 实际发现的原因
	 * @return the analysis or {@code null}
	 */
	protected abstract FailureAnalysis analyze(Throwable rootFailure, T cause);

	/**
	 * Return the cause type being handled by the analyzer. By default the class generic
	 * is used.
	 * @return the cause type
	 */
	@SuppressWarnings("unchecked")
	protected Class<? extends T> getCauseType() {
		// 故障分析器实现的抽象基类的子类
		return (Class<? extends T>) ResolvableType
				.forClass(AbstractFailureAnalyzer.class, getClass()).resolveGeneric();
	}

	@SuppressWarnings("unchecked")
	protected final <E extends Throwable> T findCause(Throwable failure, Class<E> type) {
		while (failure != null) {
			if (type.isInstance(failure)) {
				// 故障分析器的子类
				return (T) failure;
			}
			// 递归地向上游异常遍历
			failure = failure.getCause();
		}
		return null;
	}

}
