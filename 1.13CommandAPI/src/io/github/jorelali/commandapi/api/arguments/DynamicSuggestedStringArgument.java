package io.github.jorelali.commandapi.api.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;

@SuppressWarnings("unchecked")
public class DynamicSuggestedStringArgument implements Argument {

	@FunctionalInterface
	public interface DynamicSuggestions {
		String[] getSuggestions();
	}
	
	com.mojang.brigadier.arguments.ArgumentType<?> rawType;
	private DynamicSuggestions suggestions;
	
	/**
	 * A string argument which has suggestions which are determined at runtime
	 */
	public DynamicSuggestedStringArgument(DynamicSuggestions suggestions) {
		rawType = StringArgumentType.word();
		this.suggestions = suggestions;
	}
		
	@Override
	public <T> com.mojang.brigadier.arguments.ArgumentType<T> getRawType() {
		return (com.mojang.brigadier.arguments.ArgumentType<T>) rawType;
	}

	@Override
	public <V> Class<V> getPrimitiveType() {
		return (Class<V>) String.class;
	}

	@Override
	public boolean isSimple() {
		return true;
	}
	
	public DynamicSuggestions getDynamicSuggestions() {
		return suggestions;
	}
}
