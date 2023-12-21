/*
 *     CitizensBooks
 *     Copyright (c) 2023 @ Drăghiciu 'NicoNekoDev' Nicolae
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package ro.niconeko.astralbooks.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private final static Pattern TEXT_COMPONENT_PATTERN = Pattern.compile("\\[T](.*?)\\[/T]");

    private final static Pattern HOVER_COMPONENT_PATTERN = Pattern.compile("\\[H](.*?)\\[/H]");
    private final static Pattern RUN_COMMAND_COMPONENT_PATTERN = Pattern.compile("\\[C](.*?)\\[/C]");
    private final static Pattern SUGGEST_COMMAND_COMPONENT_PATTERN = Pattern.compile("\\[SC](.*?)\\[/SC]");
    private final static Pattern COPY_COMPONENT_PATTERN = Pattern.compile("\\[CC](.*?)\\[/CC]");
    private final static Pattern URL_COMPONENT_PATTERN = Pattern.compile("\\[URL](.*?)\\[/URL]");

    public static String noComponents(String text) {
        String splitText = splitComponents(text).toString();
        Matcher matcher = TEXT_COMPONENT_PATTERN.matcher(splitText);
        StringBuilder result = new StringBuilder();
        List<String> matches = new ArrayList<>();
        while (matcher.find())
            matches.add(matcher.group(1));
        List<String> splits = List.of(splitText.split(TEXT_COMPONENT_PATTERN.pattern()));
        for (int i = 0; i < matches.size(); i++) {
            result.append(splits.get(i));
            result.append(matches.get(i));
        }
        if (splits.size() > matches.size())
            result.append(splits.get(splits.size() - 1));
        return result.toString();
    }

    public static BaseComponent[] components(String text) {
        List<BaseComponent> components = new ArrayList<>();
        Matcher matcher = TEXT_COMPONENT_PATTERN.matcher(text);
        List<String> matches = new ArrayList<>();
        while (matcher.find())
            matches.add(matcher.group(1));
        List<String> splits = List.of(text.split(TEXT_COMPONENT_PATTERN.pattern()));
        for (int i = 0; i < matches.size(); i++) {
            components.add(new TextComponent(TextComponent.fromLegacyText(splits.get(i))));
            components.add(textComponent(matches.get(i)));
        }
        if (splits.size() > matches.size())
            components.add(new TextComponent(TextComponent.fromLegacyText(splits.get(splits.size() - 1))));
        return components.toArray(new BaseComponent[0]);
    }

    public static TextComponent textComponent(String text) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(splitComponents(text).toString()));
        Matcher hoverMatcher = HOVER_COMPONENT_PATTERN.matcher(text);
        Matcher runCommandMatcher = RUN_COMMAND_COMPONENT_PATTERN.matcher(text);
        Matcher suggestCommandMatcher = SUGGEST_COMMAND_COMPONENT_PATTERN.matcher(text);
        Matcher copyCommandMatcher = COPY_COMPONENT_PATTERN.matcher(text);
        Matcher urlCommandMatcher = URL_COMPONENT_PATTERN.matcher(text);
        if (hoverMatcher.find())
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverMatcher.group(1)))));
        if (runCommandMatcher.find())
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, runCommandMatcher.group(1)));
        else if (suggestCommandMatcher.find())
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommandMatcher.group(1)));
        else if (copyCommandMatcher.find())
            component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyCommandMatcher.group(1)));
        else if (urlCommandMatcher.find())
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, urlCommandMatcher.group(1)));
        return component;
    }

    @NotNull
    private static StringBuilder splitComponents(String text) {
        StringBuilder result = new StringBuilder();
        for (String split1 : text.split(HOVER_COMPONENT_PATTERN.pattern()))
            for (String split2 : split1.split(RUN_COMMAND_COMPONENT_PATTERN.pattern()))
                for (String split3 : split2.split(SUGGEST_COMMAND_COMPONENT_PATTERN.pattern()))
                    for (String split4 : split3.split(COPY_COMPONENT_PATTERN.pattern()))
                        for (String split5 : split4.split(URL_COMPONENT_PATTERN.pattern()))
                            result.append(split5);
        return result;
    }
}
