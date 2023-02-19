package com.timemates.backend.validation

/**
 * Abstraction for factories that construct value objects.
 * Next pattern should be applied to the factories:
 * - Factory should be in companion object that does only one thing – constructing.
 * - Validation information (like sizes or patterns) should be on the top of
 * the factories in order to better readability.
 * - After validation information comes [create] and, if needed, constants
 * with messages below the method.
 *
 * This is abstract class on purpose: to support clearness and readability of
 * value objects.
 *
 * **You should always implement a constructor for value objects, even if
 * there is no actual restrictions on given type. It will help to minimize
 * possible changes and support existing code style rules.**
 */
public abstract class SafeConstructor<Type, WrappedType> {
    /**
     * Method to construct valid instance of [Type].
     *
     * In addition, this function can transform input if needed (for example,
     * to remove multiple spaces or something like that, but it shouldn't
     * make something really different on user input to avoid misunderstanding
     * from user).
     *
     * @see ValidationScope
     * @return [Type] or fails in [ValidationScope].
     */
    context(ValidationScope)
    public abstract fun create(
        value: WrappedType
    ): Type
}

/**
 * Constructs a [T] from [W] with validation check in unsafe way. You should
 * use it only if it comes from trusted source (like database or from generator)
 *
 * @see [ValidationScope]
 * @see [SafeConstructor.create]
 * @throws [IllegalStateException] if validation failed.
 */
@Throws(IllegalStateException::class)
public fun <T, W> SafeConstructor<T, W>.createOrThrow(value: W): T {
    return with(throwingValidationScope) {
        create(value)
    }
}

// scope to reuse
private val throwingValidationScope = ValidationScope {
    throw IllegalStateException("Validation failed. ${it.string}")
}