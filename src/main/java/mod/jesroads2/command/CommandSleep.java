package mod.jesroads2.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class CommandSleep implements ICommand {

    private final ArrayList<String> alias;
    private final ArrayList<String> empty;

    public CommandSleep() {
        alias = new ArrayList<>();
        alias.add("sleep");
        empty = new ArrayList<>(0);
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    @Override
    public String getName() {
        return alias.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return alias;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        World world = server.getEntityWorld();
        if (!world.getGameRules().getBoolean("doDaylightCycle")) return;

        long current = world.getWorldTime(), diff = current % 24000, add = 24000 - diff;
        if (diff > 7689 && diff < 23459) {
            if (args.length > 0 && args[0].equals("rain")) {
                WorldInfo info = world.getWorldInfo();
                if (info.isRaining()) info.setRaining(false);
            }
            world.setWorldTime(current + add);
        } else
            sender.sendMessage(new TextComponentString(new TextComponentTranslation("tile.bed.noSleep").getFormattedText()));

    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          BlockPos pos) {
        return empty;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}