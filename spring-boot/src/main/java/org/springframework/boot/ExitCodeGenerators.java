
package org.springframework.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Maintains a collection of {@link ExitCodeGenerator} instances and allows the final exit
 * code to be calculated.
 * 维护退出代码生成器实例的容器，并允许计算最终退出代码。
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @see #getExitCode()
 * @see ExitCodeGenerator
 */
class ExitCodeGenerators implements Iterable<ExitCodeGenerator> {

	/**
	 * 退出代码生成器列表
	 */
	private List<ExitCodeGenerator> generators = new ArrayList<ExitCodeGenerator>();

	public void addAll(Throwable exception, ExitCodeExceptionMapper... mappers) {
		Assert.notNull(exception, "Exception must not be null");
		Assert.notNull(mappers, "Mappers must not be null");
		addAll(exception, Arrays.asList(mappers));
	}

	public void addAll(Throwable exception,
			Iterable<? extends ExitCodeExceptionMapper> mappers) {
		Assert.notNull(exception, "Exception must not be null");
		Assert.notNull(mappers, "Mappers must not be null");
		for (ExitCodeExceptionMapper mapper : mappers) {
			add(exception, mapper);
		}
	}

	public void add(Throwable exception, ExitCodeExceptionMapper mapper) {
		Assert.notNull(exception, "Exception must not be null");
		Assert.notNull(mapper, "Mapper must not be null");
		add(new MappedExitCodeGenerator(exception, mapper));
	}

	public void addAll(ExitCodeGenerator... generators) {
		Assert.notNull(generators, "Generators must not be null");
		addAll(Arrays.asList(generators));
	}

	public void addAll(Iterable<? extends ExitCodeGenerator> generators) {
		Assert.notNull(generators, "Generators must not be null");
		for (ExitCodeGenerator generator : generators) {
			add(generator);
		}
	}

	public void add(ExitCodeGenerator generator) {
		Assert.notNull(generator, "Generator must not be null");
		this.generators.add(generator);
	}

	@Override
	public Iterator<ExitCodeGenerator> iterator() {
		return this.generators.iterator();
	}

	/**
	 * Get the final exit code that should be returned based on all contained generators.
	 * @return the final exit code.
	 */
	public int getExitCode() {
		int exitCode = 0;
		for (ExitCodeGenerator generator : this.generators) {
			try {
				// 生成器的退出代码
				int value = generator.getExitCode();
				if (value > 0 && value > exitCode || value < 0 && value < exitCode) {
					exitCode = value;
				}
			}
			catch (Exception ex) {
				exitCode = (exitCode != 0) ? exitCode : 1;
//				ex.printStackTrace();
			}
		}
		return exitCode;
	}

	/**
	 * Adapts an {@link ExitCodeExceptionMapper} to an {@link ExitCodeGenerator}.
	 */
	private static class MappedExitCodeGenerator implements ExitCodeGenerator {

		/**
		 * 退出异常
		 */
		private final Throwable exception;

		/**
		 * 异常和退出代码之间的映射
		 */
		private final ExitCodeExceptionMapper mapper;

		MappedExitCodeGenerator(Throwable exception, ExitCodeExceptionMapper mapper) {
			this.exception = exception;
			this.mapper = mapper;
		}

		@Override
		public int getExitCode() {
			return this.mapper.getExitCode(this.exception);
		}

	}

}
