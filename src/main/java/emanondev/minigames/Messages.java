package emanondev.minigames;

import org.bukkit.command.CommandSender;

@Deprecated
public class Messages {


    public static class Minifiller {
        public static class Gui {


        }

        public static class Error {
            private static final String PREFIX = "minifiller.error.";


            public static final void CREATE_ARGUMENTS_AMOUNT(CommandSender sender, String label) {
                MessageUtil.sendMessage(sender, PREFIX + "create_arguments_amount",
                        "%label%", label);
            }

            public static final void INVALID_ID(CommandSender sender, String label, String id) {
                MessageUtil.sendMessage(sender, PREFIX + "invalid_id",
                        "%label%", label, "%id", id);
            }

            public static final void ALREADY_USED_ID(CommandSender sender, String label, String id) {
                MessageUtil.sendMessage(sender, PREFIX + "already_used_id",
                        "%label%", label, "%id", id);
            }
        }


    }
}
