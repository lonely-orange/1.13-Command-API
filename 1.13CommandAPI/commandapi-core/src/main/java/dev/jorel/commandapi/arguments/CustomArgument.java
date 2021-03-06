package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import dev.jorel.commandapi.CommandAPIHandler;

/**
 * An argument that represents any custom object
 *
 * @param <T> the return type of this argument
 */
public class CustomArgument<T> extends Argument {
	
	private CustomArgumentParser<T> parser;
	
	/**
	 * Creates a CustomArgument with a valid parser, defaults to non-keyed argument
	 * 
	 * @param parser
	 *            A CustomArgumentParser<T> that maps a String to the object of your choice.
	 *            The String input is the text that the CommandSender inputs for
	 *            this argument
	 *            
	 * @see #CustomArgument(CustomArgumentParser<T>, boolean)
	 */
	public CustomArgument(CustomArgumentParser<T> parser) {
		this(parser, false);
	}
	
	/**
	 * Creates a CustomArgument with a valid parser
	 * 
	 * @param parser
	 *            A CustomArgumentParser that maps a String to the object of your choice.
	 *            The String input is the text that the CommandSender inputs for
	 *            this argument
	 * @param keyed Whether this argument can accept Minecraft's <code>NamespacedKey</code> as
	 * valid arguments
	 */
	public CustomArgument(CustomArgumentParser<T> parser, boolean keyed) {
		super(keyed ? CommandAPIHandler.getNMS()._ArgumentMinecraftKeyRegistered() : StringArgumentType.string());
		this.parser = parser;
	}

	@Override
	public Class<T> getPrimitiveType() {
		return null;
	}
	
	/**
	 * Returns the parser for this custom argument
	 * @return the parser for this custom argument
	 */
	public CustomArgumentParser<T> getParser() {
		return parser;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.CUSTOM;
	}
	
	/**
	 * MessageBuilder is used to create error messages for invalid argument inputs
	 */
	public static class MessageBuilder {
		StringBuilder builder;
		
		/**
		 * Create a blank message
		 */
		public MessageBuilder() {
			builder = new StringBuilder();
		}
		
		/**
		 * Create a message with an input string
		 * @param str The string to start the message with
		 */
		public MessageBuilder(String str) {
			builder = new StringBuilder(str);
		}
		
		/**
		 * Appends the argument input that the CommandSender used in this command.<br>
		 * For example, if <code>/foo bar</code> was executed and an error occurs
		 * with the CustomArgument <code>bar</code>, then the arg input will append
		 * <code>bar</code> to the end of the message.<br><br> This input is determined
		 * at runtime, and is stored as <code>%input%</code> until executed 
		 * @return A reference to this object
		 */
		public MessageBuilder appendArgInput() {
			builder.append("%input%");
			return this;
		}
		
		/**
		 * Appends the whole input that the CommandSender used in this command.<br>
		 * For example, if <code>/foo bar</code> was executed, then <code>foo bar</code> will be
		 * appended to the end of the message.<br><br> This input is determined
		 * at runtime, and is stored as <code>%finput%</code> until executed 
		 * @return A reference to this object
		 */
		public MessageBuilder appendFullInput() {
			builder.append("%finput%");
			return this;
		}
		
		/**
		 * Appends <code><--[HERE]</code> to the end of the message
		 * @return A reference to this object
		 */
		public MessageBuilder appendHere() {
			builder.append("<--[HERE]");
			return this;
		}
		
		/**
		 * Appends a string to the end of this message
		 * @param str The string to append to the end of this message
		 * @return A reference to this object
		 */
		public MessageBuilder append(String str) {
			builder.append(str);
			return this;
		}
		
		/**
		 * Appends an object to the end of this message
		 * @param obj The object to append to the end of this message
		 * @return A reference to this object
		 */
		public MessageBuilder append(Object obj) {
			builder.append(obj);
			return this;
		}
		
		@Override
		public String toString() {
			return builder.toString();
		}
	}
	
	/**
	 * An exception used to create command-related errors for the CustomArgument
	 */
	@SuppressWarnings("serial")
	public static class CustomArgumentException extends Exception {

		final String errorMessage;
		final MessageBuilder errorMessageBuilder;
		
		public CustomArgumentException(String errorMessage) {
			this.errorMessage = errorMessage;
			this.errorMessageBuilder = null;
		}
		
		public CustomArgumentException(MessageBuilder errorMessage) {
			this.errorMessage = null;
			this.errorMessageBuilder = errorMessage;
		}
		
		public CommandSyntaxException toCommandSyntax(String result, CommandContext<?> cmdCtx) {
			if(errorMessage == null) {
				//Deal with MessageBuilder
				String errorMsg = errorMessageBuilder.toString().replace("%input%", result).replace("%finput%", cmdCtx.getInput());
				return new SimpleCommandExceptionType(() -> {return errorMsg;}).create();
			} else {
				//Deal with String
				return new SimpleCommandExceptionType(new LiteralMessage(errorMessage)).create();
			}
		}
		
	}
	
	/**
	 * A FunctionalInterface that takes in a String, returns T and can throw a CustomArgumentException
	 * 
	 * @param <T> the type that is returned when applying this parser
	 */
	@FunctionalInterface
	public static interface CustomArgumentParser<T> {
		public T apply(String input) throws CustomArgumentException;
	}
}
