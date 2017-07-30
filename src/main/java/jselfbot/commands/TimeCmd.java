/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jselfbot.commands;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jselfbot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * @author John Grosh (jagrosh)
 */
public class TimeCmd extends Command {
    private final ZoneId zone;

    public TimeCmd(ZoneId zone) {
        this.zone = zone;
        this.name = "time";
        this.description = "checks the time";
        this.type = Type.EDIT_ORIGINAL;
    }

    @Override
    protected void execute(String args, MessageReceivedEvent event) {
        ZonedDateTime t = event.getMessage().getCreationTime().atZoneSameInstant(zone);
        String time = t.format(DateTimeFormatter.ofPattern("h:mma"));
        String time24 = t.format(DateTimeFormatter.ofPattern("HH:mm"));
        String name = event.getGuild() == null ? event.getAuthor().getName() : event.getMember().getEffectiveName();
        reply("\u231A Current time for **" + name + "** is `" + time + "` (`" + time24 + "`)", event);
    }

}
