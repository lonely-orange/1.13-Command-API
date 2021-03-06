package dev.jorel.commandapi.nms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_13_R1.CraftLootTable;
import org.bukkit.craftbukkit.v1_13_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.CraftSound;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_13_R1.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_13_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R1.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;

import de.tr7zw.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.ICustomProvidedArgument.SuggestionProviders;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.exceptions.BiomeArgumentException;
import dev.jorel.commandapi.exceptions.EnvironmentArgumentException;
import dev.jorel.commandapi.exceptions.TimeArgumentException;
import dev.jorel.commandapi.wrappers.FloatRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_13_R1.Advancement;
import net.minecraft.server.v1_13_R1.ArgumentChat;
import net.minecraft.server.v1_13_R1.ArgumentChatComponent;
import net.minecraft.server.v1_13_R1.ArgumentChatFormat;
import net.minecraft.server.v1_13_R1.ArgumentCriterionValue;
import net.minecraft.server.v1_13_R1.ArgumentEnchantment;
import net.minecraft.server.v1_13_R1.ArgumentEntity;
import net.minecraft.server.v1_13_R1.ArgumentEntitySummon;
import net.minecraft.server.v1_13_R1.ArgumentItemStack;
import net.minecraft.server.v1_13_R1.ArgumentMathOperation;
import net.minecraft.server.v1_13_R1.ArgumentMinecraftKeyRegistered;
import net.minecraft.server.v1_13_R1.ArgumentMobEffect;
import net.minecraft.server.v1_13_R1.ArgumentNBTTag;
import net.minecraft.server.v1_13_R1.ArgumentParticle;
import net.minecraft.server.v1_13_R1.ArgumentPosition;
import net.minecraft.server.v1_13_R1.ArgumentProfile;
import net.minecraft.server.v1_13_R1.ArgumentRotation;
import net.minecraft.server.v1_13_R1.ArgumentRotationAxis;
import net.minecraft.server.v1_13_R1.ArgumentScoreboardCriteria;
import net.minecraft.server.v1_13_R1.ArgumentScoreboardObjective;
import net.minecraft.server.v1_13_R1.ArgumentScoreboardSlot;
import net.minecraft.server.v1_13_R1.ArgumentScoreboardTeam;
import net.minecraft.server.v1_13_R1.ArgumentScoreholder;
import net.minecraft.server.v1_13_R1.ArgumentTag;
import net.minecraft.server.v1_13_R1.ArgumentTile;
import net.minecraft.server.v1_13_R1.ArgumentVec2;
import net.minecraft.server.v1_13_R1.ArgumentVec3;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.CommandListenerWrapper;
import net.minecraft.server.v1_13_R1.CompletionProviders;
import net.minecraft.server.v1_13_R1.CriterionConditionValue;
import net.minecraft.server.v1_13_R1.CustomFunction;
import net.minecraft.server.v1_13_R1.CustomFunctionData;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.EnumDirection.EnumAxis;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.ICompletionProvider;
import net.minecraft.server.v1_13_R1.IVectorPosition;
import net.minecraft.server.v1_13_R1.LootTableRegistry;
import net.minecraft.server.v1_13_R1.MinecraftKey;
import net.minecraft.server.v1_13_R1.MinecraftServer;
import net.minecraft.server.v1_13_R1.ScoreboardScore;
import net.minecraft.server.v1_13_R1.Vec2F;
import net.minecraft.server.v1_13_R1.Vec3D;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NMS_1_13 implements NMS {
    @Override
    public ArgumentType<?> _ArgumentAxis() {
        return ArgumentRotationAxis.a();
    }
    
    @Override
	public ArgumentType<?> _ArgumentBlockState() {
		return ArgumentTile.a();
	}

    @Override
    public ArgumentType<?> _ArgumentChat() {
        return ArgumentChat.a();
    }

    @Override
    public ArgumentType _ArgumentChatComponent() {
        return ArgumentChatComponent.a();
    }

    @Override
    public ArgumentType _ArgumentChatFormat() {
        return ArgumentChatFormat.a();
    }

    @Override
    public ArgumentType<?> _ArgumentDimension() {
        throw new EnvironmentArgumentException();
    }

    @Override
    public ArgumentType _ArgumentEnchantment() {
        return ArgumentEnchantment.a();
    }

    @Override
    public ArgumentType _ArgumentEntity(EntitySelector selector) {
        switch (selector) {
            case MANY_ENTITIES:
                return ArgumentEntity.b();
            case MANY_PLAYERS:
                return ArgumentEntity.d();
            case ONE_ENTITY:
                return ArgumentEntity.a();
            case ONE_PLAYER:
                return ArgumentEntity.c();
        }
        return null;
    }

    @Override
    public ArgumentType _ArgumentEntitySummon() {
        return ArgumentEntitySummon.a();
    }

    @Override
    public ArgumentType<?> _ArgumentFloatRange() {
        return new ArgumentCriterionValue.a();
    }

    @Override
    public ArgumentType<?> _ArgumentIntRange() {
        return new ArgumentCriterionValue.b();
    }

    @Override
    public ArgumentType _ArgumentItemStack() {
        return ArgumentItemStack.a();
    }

    @Override
    public ArgumentType<?> _ArgumentMathOperation() {
        return ArgumentMathOperation.a();
    }

    @Override
    public ArgumentType _ArgumentMinecraftKeyRegistered() {
        return ArgumentMinecraftKeyRegistered.a();
    }

    @Override
    public ArgumentType _ArgumentMobEffect() {
        return ArgumentMobEffect.a();
    }

    @Override
    public ArgumentType<?> _ArgumentNBTCompound() {
        return ArgumentNBTTag.a();
    }

    @Override
    public ArgumentType _ArgumentParticle() {
        return ArgumentParticle.a();
    }

    @Override
    public ArgumentType _ArgumentPosition() {
        return ArgumentPosition.a();
    }

    @Override
    public ArgumentType<?> _ArgumentPosition2D() {
        return ArgumentVec2.a();
    }

    @Override
    public ArgumentType _ArgumentProfile() {
        return ArgumentProfile.a();
    }

    @Override
    public ArgumentType<?> _ArgumentRotation() {
        return ArgumentRotation.a();
    }

    @Override
    public ArgumentType<?> _ArgumentScoreboardCriteria() {
        return ArgumentScoreboardCriteria.a();
    }

    @Override
    public ArgumentType<?> _ArgumentScoreboardObjective() {
        return ArgumentScoreboardObjective.a();
    }

    @Override
    public ArgumentType<?> _ArgumentScoreboardSlot() {
        return ArgumentScoreboardSlot.a();
    }

    @Override
    public ArgumentType<?> _ArgumentScoreboardTeam() {
        return ArgumentScoreboardTeam.a();
    }

    @Override
    public ArgumentType<?> _ArgumentScoreholder(boolean single) {
        return single
               ? ArgumentScoreholder.a()
               : ArgumentScoreholder.b();
    }

    @Override
    public ArgumentType _ArgumentTag() {
        return ArgumentTag.a();
    }

    @Override
    public ArgumentType<?> _ArgumentTime() {
        throw new TimeArgumentException();
    }

    @Override
    public ArgumentType<?> _ArgumentVec2() {
        return ArgumentVec2.a();
    }

    @Override
    public ArgumentType _ArgumentVec3() {
        return ArgumentVec3.a();
    }

    @Override
    public String[] compatibleVersions() {
        return new String[]{ "1.13" };
    }

    @Override
    public void createDispatcherFile(Object server, File file, CommandDispatcher dispatcher) {
        ((MinecraftServer) server).commandDispatcher.a(file);
    }

    @Override
    public org.bukkit.advancement.Advancement getAdvancement(CommandContext cmdCtx, String key) throws CommandSyntaxException {
        return ArgumentMinecraftKeyRegistered.a(cmdCtx, key).bukkit;
    }

    @Override
    public EnumSet<Axis> getAxis(CommandContext cmdCtx, String key) {
        EnumSet<Axis> set = EnumSet.noneOf(Axis.class);
        EnumSet<EnumAxis> parsedEnumSet = ArgumentRotationAxis.a(cmdCtx, key);
        for (EnumAxis element : parsedEnumSet) {
            switch (element) {
                case X:
                    set.add(Axis.X);
                    break;
                case Y:
                    set.add(Axis.Y);
                    break;
                case Z:
                    set.add(Axis.Z);
                    break;
            }
        }
        return set;
    }

    @Override
	public Biome getBiome(CommandContext cmdCtx, String key) {
		throw new BiomeArgumentException();
	}

    @Override
	public BlockData getBlockState(CommandContext cmdCtx, String key) {
		return CraftBlockData.fromData(ArgumentTile.a(cmdCtx, key).a());
	}

    @Override
    public CommandDispatcher getBrigadierDispatcher(Object server) {
        return ((MinecraftServer) server).commandDispatcher.a();
    }

    @Override
    public BaseComponent[] getChat(CommandContext cmdCtx, String key) throws CommandSyntaxException {
        String resultantString = ChatSerializer.a(ArgumentChat.a(cmdCtx, key));
        return ComponentSerializer.parse(resultantString);
    }

    @Override
    public ChatColor getChatColor(CommandContext cmdCtx, String str) {
        return CraftChatMessage.getColor(ArgumentChatFormat.a(cmdCtx, str));
    }

    @Override
    public BaseComponent[] getChatComponent(CommandContext cmdCtx, String str) {
        String resultantString = ChatSerializer.a(ArgumentChatComponent.a(cmdCtx, str));
        return ComponentSerializer.parse(resultantString);
    }

    private CommandListenerWrapper getCLW(CommandContext cmdCtx) {
        return (CommandListenerWrapper) cmdCtx.getSource();
    }

    @Override
    public CommandSender getCommandSenderForCLW(Object clw) {
        return ((CommandListenerWrapper) clw).getBukkitSender();
    }

    @Override
    public Environment getDimension(CommandContext<?> cmdCtx, String key) {
        throw new EnvironmentArgumentException();
    }

    @Override
    public Enchantment getEnchantment(CommandContext cmdCtx, String str) {
        return new CraftEnchantment(ArgumentEnchantment.a(cmdCtx, str));
    }

    @Override
    public Object getEntitySelector(CommandContext cmdCtx, String str, EntitySelector selector) throws CommandSyntaxException {
        switch (selector) {
            case MANY_ENTITIES:
                try {
                    return ArgumentEntity.c(cmdCtx, str).stream().map(entity -> ((Entity) entity).getBukkitEntity()).collect(Collectors.toList());
                } catch (CommandSyntaxException e) {
                    return new ArrayList<org.bukkit.entity.Entity>();
                }
            case MANY_PLAYERS:
                try {
                    return ArgumentEntity.d(cmdCtx, str).stream().map(player -> ((Entity) player).getBukkitEntity()).collect(Collectors.toList());
                } catch (CommandSyntaxException e) {
                    return new ArrayList<Player>();
                }
            case ONE_ENTITY:
                return ArgumentEntity.a(cmdCtx, str).getBukkitEntity();
            case ONE_PLAYER:
                return ArgumentEntity.e(cmdCtx, str).getBukkitEntity();
        }
        return null;
    }

    @Override
    public EntityType getEntityType(CommandContext cmdCtx, String str, CommandSender sender) throws CommandSyntaxException {
        Entity entity = EntityTypes.a(((CraftWorld) getCommandSenderWorld(sender)).getHandle(), ArgumentEntitySummon.a(cmdCtx, str));
        return entity.getBukkitEntity().getType();
    }

    @Override
    public FloatRange getFloatRange(CommandContext<?> cmdCtx, String key) {
        CriterionConditionValue.c range = cmdCtx.getArgument(key, CriterionConditionValue.c.class);
        float low = range.a() == null
                    ? -Float.MAX_VALUE
                    : range.a();
        float high = range.b() == null
                     ? Float.MAX_VALUE
                     : range.b();
        return new FloatRange(low, high);
    }

    @Override
    public FunctionWrapper[] getFunction(CommandContext cmdCtx, String str) throws CommandSyntaxException {
        Collection<CustomFunction> customFuncList = ArgumentTag.a(cmdCtx, str);

        FunctionWrapper[] result = new FunctionWrapper[customFuncList.size()];

        CustomFunctionData customFunctionData = getCLW(cmdCtx).getServer().getFunctionData();
        CommandListenerWrapper commandListenerWrapper = getCLW(cmdCtx).a().b(2);

        int count = 0;
        for(CustomFunction customFunction : customFuncList) {
            @SuppressWarnings("deprecation")
            NamespacedKey minecraftKey = new NamespacedKey(customFunction.a().b(), customFunction.a().getKey());
            ToIntBiFunction<CustomFunction, CommandListenerWrapper> obj = customFunctionData::a;

            FunctionWrapper wrapper = new FunctionWrapper(minecraftKey, obj, customFunction, commandListenerWrapper, e -> {
                return getCLW(cmdCtx).a(((CraftEntity) e).getHandle());
            });

            result[count] = wrapper;
            count++;
        }

        return result;
    }

    @Override
    public IntegerRange getIntRange(CommandContext cmdCtx, String key) {
        net.minecraft.server.v1_13_R1.CriterionConditionValue.d range = ArgumentCriterionValue.b.a(cmdCtx, key);
        int low = range.a() == null
                  ? Integer.MIN_VALUE
                  : range.a();
        int high = range.b() == null
                   ? Integer.MAX_VALUE
                   : range.b();
        return new IntegerRange(low, high);
    }

    @Override
    public ItemStack getItemStack(CommandContext cmdCtx, String str) throws CommandSyntaxException {
        return CraftItemStack.asBukkitCopy(ArgumentItemStack.a(cmdCtx, str).a(1, false));
    }

    @Override
    public Location getLocation(CommandContext cmdCtx, String str, LocationType locationType, CommandSender sender) throws CommandSyntaxException {
        switch (locationType) {
            case BLOCK_POSITION:
                BlockPosition blockPos = ArgumentPosition.a(cmdCtx, str);
                return new Location(getCommandSenderWorld(sender), blockPos.getX(), blockPos.getY(), blockPos.getZ());
            case PRECISE_POSITION:
                Vec3D vecPos = ArgumentVec3.a(cmdCtx, str);
                return new Location(getCommandSenderWorld(sender), vecPos.x, vecPos.y, vecPos.z);
        }
        return null;
    }

    @Override
    public Location2D getLocation2D(CommandContext cmdCtx, String key, LocationType locationType2d, CommandSender sender) throws CommandSyntaxException {
        switch (locationType2d) {
            case BLOCK_POSITION: {
                Vec2F vecPos = ArgumentVec2.a(cmdCtx, key);
                return new Location2D(getCommandSenderWorld(sender), Math.round(vecPos.i), Math.round(vecPos.j));
            }
            case PRECISE_POSITION: {
                Vec2F vecPos = ArgumentVec2.a(cmdCtx, key);
                return new Location2D(getCommandSenderWorld(sender), vecPos.i, vecPos.j);
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public LootTable getLootTable(CommandContext cmdCtx, String str) {
        MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.c(cmdCtx, str);
        String namespace = minecraftKey.b();
        String key = minecraftKey.getKey();

        net.minecraft.server.v1_13_R1.LootTable lootTable = getCLW(cmdCtx).getServer().aP().a(minecraftKey);
        return new CraftLootTable(new NamespacedKey(namespace, key), lootTable);
    }

    @Override
    public MathOperation getMathOperation(CommandContext cmdCtx, String key) throws CommandSyntaxException {
    	ArgumentMathOperation.a result = ArgumentMathOperation.a(cmdCtx, key);
    	net.minecraft.server.v1_13_R1.Scoreboard board = new net.minecraft.server.v1_13_R1.Scoreboard();
    	ScoreboardScore tester_left = new ScoreboardScore(board, null, null);
    	ScoreboardScore tester_right = new ScoreboardScore(board, null, null);
    	
    	tester_left.setScore(6);
    	tester_right.setScore(2);
    	result.apply(tester_left, tester_right);
    	
    	switch (tester_left.getScore()) {
    		case 8: return MathOperation.ADD;
    		case 4: return MathOperation.SUBTRACT;
    		case 12: return MathOperation.MULTIPLY;
    		case 3: return MathOperation.DIVIDE;
    		case 0: return MathOperation.MOD;
    		case 6: return MathOperation.MAX;
    		
    		case 2: {
    			if (tester_right.getScore() == 6)
    				return MathOperation.SWAP;
    			tester_left.setScore(2);
    			tester_right.setScore(6);
    			result.apply(tester_left, tester_right);
    			if (tester_left.getScore() == 2)
    				return MathOperation.MIN;
    			return MathOperation.ASSIGN;
    		}
    	}
    	return null;
    }

    @Override
    public NBTContainer getNBTCompound(CommandContext<?> cmdCtx, String key) {
        return new NBTContainer(ArgumentNBTTag.a(cmdCtx, key));
    }

    @Override
    public String getObjective(CommandContext cmdCtx, String key, CommandSender sender) throws IllegalArgumentException, CommandSyntaxException {
        return ArgumentScoreboardObjective.a(cmdCtx, key).getName();
    }

    @Override
    public String getObjectiveCriteria(CommandContext cmdCtx, String key) {
        return ArgumentScoreboardCriteria.a(cmdCtx, key).getName();
    }

    @Override
    public Particle getParticle(CommandContext cmdCtx, String str) {
        return CraftParticle.toBukkit(ArgumentParticle.a(cmdCtx, str));
    }

    @Override
    public Player getPlayer(CommandContext cmdCtx, String str) throws CommandSyntaxException {
        Player target = Bukkit.getPlayer(((GameProfile) ArgumentProfile.a(cmdCtx, str).iterator().next()).getId());
        if (target == null) {
            throw ArgumentProfile.a.create();
        } else {
            return target;
        }
    }

    @Override
    public PotionEffectType getPotionEffect(CommandContext cmdCtx, String str) throws CommandSyntaxException {
        return new CraftPotionEffectType(ArgumentMobEffect.a(cmdCtx, str));
    }

    @Override
    public Recipe getRecipe(CommandContext cmdCtx, String key) throws CommandSyntaxException {
        return ArgumentMinecraftKeyRegistered.b(cmdCtx, key).toBukkitRecipe();
    }

    @Override
    public Rotation getRotation(CommandContext cmdCtx, String key) {
        IVectorPosition pos = ArgumentRotation.a(cmdCtx, key);
        Vec2F vec = pos.b(getCLW(cmdCtx));
        return new Rotation(vec.i, vec.j);
    }

    @Override
    public ScoreboardSlot getScoreboardSlot(CommandContext cmdCtx, String key) {
        return new ScoreboardSlot(ArgumentScoreboardSlot.a(cmdCtx, key));
    }

    @Override
    public Collection<String> getScoreHolderMultiple(CommandContext cmdCtx, String key) throws CommandSyntaxException {
        return ArgumentScoreholder.b(cmdCtx, key);
    }

    @Override
    public String getScoreHolderSingle(CommandContext cmdCtx, String key) throws CommandSyntaxException {
        return ArgumentScoreholder.a(cmdCtx, key);
    }

    @Override
    public CommandSender getSenderForCommand(CommandContext cmdCtx) {
        CommandSender sender = getCLW(cmdCtx).getBukkitSender();

        Entity proxyEntity = getCLW(cmdCtx).f();
        if (proxyEntity != null) {
            CommandSender proxy = proxyEntity.getBukkitEntity();

            if (!proxy.equals(sender)) {
                sender = new ProxiedNativeCommandSender(getCLW(cmdCtx), sender, proxy);
            }
        }

        return sender;
    }

    @Override
    public SimpleCommandMap getSimpleCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public Sound getSound(CommandContext cmdCtx, String key) {
    	MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.c(cmdCtx, key);
		for(CraftSound sound : CraftSound.values()) {
			try {
				if(CommandAPIHandler.getField(CraftSound.class, "minecraftKey").get(sound).equals(minecraftKey.getKey())) {
					return Sound.valueOf(sound.name());
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
		return null;
    }

    @Override
    public SuggestionProvider getSuggestionProvider(SuggestionProviders provider) {
        switch (provider) {
            case FUNCTION:
                return (context, builder) -> {
                    CustomFunctionData functionData = getCLW(context).getServer().getFunctionData();
                    ICompletionProvider.a(functionData.g().a(), builder, "#");
                    return ICompletionProvider.a(functionData.c().keySet(), builder);
                };
            case RECIPES:
                return CompletionProviders.b;
            case SOUNDS:
                return CompletionProviders.c;
            case ADVANCEMENTS:
                return (cmdCtx, builder) -> {
                    Collection<Advancement> advancements = ((CommandListenerWrapper) cmdCtx.getSource()).getServer().getAdvancementData().b();
                    return ICompletionProvider.a(advancements.stream().map(Advancement::getName)::iterator, builder);
                };
            case LOOT_TABLES:
                return (context, builder) -> {
                    try {
                        Map<MinecraftKey, LootTable> map = (Map<MinecraftKey, LootTable>) CommandAPIHandler.getField(LootTableRegistry.class, "e").get(getCLW(context).getServer().aP());
                        return ICompletionProvider.a((Iterable) map.keySet(), builder);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return Suggestions.empty();
                };
            case ENTITIES:
    			return CompletionProviders.d;
    		case BIOMES:
            default:
                return (context, builder) -> Suggestions.empty();
        }
    }

    @Override
    public String getTeam(CommandContext cmdCtx, String key, CommandSender sender) throws CommandSyntaxException {
        return ArgumentScoreboardTeam.a(cmdCtx, key).getName();
    }

    @Override
    public int getTime(CommandContext<?> cmdCtx, String key) {
        throw new TimeArgumentException();
    }

	@Override
    public boolean isVanillaCommandWrapper(Command command) {
        return command instanceof VanillaCommandWrapper;
    }

	@Override
    public void resendPackets(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        net.minecraft.server.v1_13_R1.CommandDispatcher nmsDispatcher = craftServer.getServer().commandDispatcher;
        nmsDispatcher.a(craftPlayer.getHandle());
    }

	@Override
	public boolean validateMinecraftKeyRegistered(String argument) {
		try {
			StringReader reader = new StringReader(argument);
			ArgumentMinecraftKeyRegistered.a().parse(reader);
			return true;
		} catch (CommandSyntaxException e) {
			return false;
		}
	}
}
