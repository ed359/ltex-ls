/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.DiagnosticSeverity;

public class Settings {
  private static final Set<String> defaultEnabled = new HashSet<>(Arrays.asList(
      "bibtex", "latex", "markdown", "org", "restructuredtext", "rsweave"));

  private @Nullable Set<String> enabled;
  private @Nullable String languageShortCode;
  private @Nullable Map<String, Set<String>> dictionary;
  private @Nullable Map<String, Set<String>> disabledRules;
  private @Nullable Map<String, Set<String>> enabledRules;
  private @Nullable Map<String, Set<HiddenFalsePositive>> hiddenFalsePositives;
  private @Nullable Map<String, Boolean> bibtexFields;
  private @Nullable Map<String, String> latexCommands;
  private @Nullable Map<String, String> latexEnvironments;
  private @Nullable Map<String, String> markdownNodes;
  private @Nullable Boolean enablePickyRules;
  private @Nullable String motherTongueShortCode;
  private @Nullable String languageModelRulesDirectory;
  private @Nullable String neuralNetworkModelRulesDirectory;
  private @Nullable String word2VecModelRulesDirectory;
  private @Nullable String languageToolHttpServerUri;
  private @Nullable Level logLevel;
  private @Nullable Integer sentenceCacheSize;
  private @Nullable DiagnosticSeverity diagnosticSeverity;
  private @Nullable CheckFrequency checkFrequency;
  private @Nullable Boolean clearDiagnosticsWhenClosingFile;

  public Settings() {
    this.enabled = null;
    this.languageShortCode = null;
    this.dictionary = null;
    this.disabledRules = null;
    this.enabledRules = null;
    this.hiddenFalsePositives = null;
    this.bibtexFields = null;
    this.latexCommands = null;
    this.latexEnvironments = null;
    this.markdownNodes = null;
    this.enablePickyRules = null;
    this.motherTongueShortCode = null;
    this.languageModelRulesDirectory = null;
    this.neuralNetworkModelRulesDirectory = null;
    this.word2VecModelRulesDirectory = null;
    this.languageToolHttpServerUri = null;
    this.logLevel = null;
    this.sentenceCacheSize = null;
    this.diagnosticSeverity = null;
    this.checkFrequency = null;
    this.clearDiagnosticsWhenClosingFile = null;
  }

  public Settings(Settings obj) {
    this.enabled = obj.enabled;
    this.languageShortCode = obj.languageShortCode;
    this.dictionary = ((obj.dictionary == null) ? null : copyMapOfSets(obj.dictionary));
    this.disabledRules = ((obj.disabledRules == null) ? null : copyMapOfSets(obj.disabledRules));
    this.enabledRules = ((obj.enabledRules == null) ? null : copyMapOfSets(obj.enabledRules));
    this.bibtexFields = ((obj.bibtexFields == null) ? null
        : new HashMap<>(obj.bibtexFields));
    this.latexCommands = ((obj.latexCommands == null) ? null
        : new HashMap<>(obj.latexCommands));
    this.latexEnvironments = ((obj.latexEnvironments == null) ? null
        : new HashMap<>(obj.latexEnvironments));
    this.markdownNodes = ((obj.markdownNodes == null) ? null
        : new HashMap<>(obj.markdownNodes));
    this.hiddenFalsePositives = ((obj.hiddenFalsePositives == null) ? null
        : new HashMap<>(obj.hiddenFalsePositives));
    this.enablePickyRules = obj.enablePickyRules;
    this.motherTongueShortCode = obj.motherTongueShortCode;
    this.languageModelRulesDirectory = obj.languageModelRulesDirectory;
    this.neuralNetworkModelRulesDirectory = obj.neuralNetworkModelRulesDirectory;
    this.word2VecModelRulesDirectory = obj.word2VecModelRulesDirectory;
    this.languageToolHttpServerUri = obj.languageToolHttpServerUri;
    this.logLevel = obj.logLevel;
    this.sentenceCacheSize = obj.sentenceCacheSize;
    this.diagnosticSeverity = ((obj.diagnosticSeverity == null) ? null : obj.diagnosticSeverity);
    this.checkFrequency = ((obj.checkFrequency == null) ? null : obj.checkFrequency);
    this.clearDiagnosticsWhenClosingFile = ((obj.clearDiagnosticsWhenClosingFile == null) ? null
        : obj.clearDiagnosticsWhenClosingFile);
  }

  public Settings(JsonElement jsonSettings, @Nullable JsonElement jsonWorkspaceSpecificSettings) {
    setSettings(jsonSettings, jsonWorkspaceSpecificSettings);
  }

  private static Map<String, Set<String>> copyMapOfSets(Map<String, Set<String>> map) {
    Map<String, Set<String>> mapCopy = new HashMap<>();

    for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
      mapCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }

    return mapCopy;
  }

  private static JsonElement getSettingFromJson(JsonElement jsonSettings, String name) {
    for (String component : name.split("\\.")) {
      jsonSettings = jsonSettings.getAsJsonObject().get(component);
    }

    return jsonSettings;
  }

  private static List<String> convertJsonArrayToList(JsonArray array) {
    List<String> list = new ArrayList<>();
    for (JsonElement element : array) list.add(element.getAsString());
    return list;
  }

  private static Set<String> convertJsonArrayToSet(JsonArray array) {
    Set<String> set = new HashSet<>();
    for (JsonElement element : array) set.add(element.getAsString());
    return set;
  }

  private static Map<String, String> convertJsonObjectToMapOfStrings(JsonObject object) {
    Map<String, String> map = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      map.put(entry.getKey(), entry.getValue().getAsString());
    }

    return map;
  }

  private static Map<String, Boolean> convertJsonObjectToMapOfBooleans(JsonObject object) {
    Map<String, Boolean> map = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      map.put(entry.getKey(), entry.getValue().getAsBoolean());
    }

    return map;
  }

  private static Map<String, List<String>> convertJsonObjectToMapOfLists(JsonObject object) {
    Map<String, List<String>> map = new HashMap<>();

    for (String key : object.keySet()) {
      map.put(key, convertJsonArrayToList(object.get(key).getAsJsonArray()));
    }

    return map;
  }

  private static void mergeMapOfListsIntoMapOfSets(Map<String, Set<String>> map1,
        Map<String, List<String>> map2) {
    for (Map.Entry<String, List<String>> entry2 : map2.entrySet()) {
      String key = entry2.getKey();
      if (!map1.containsKey(key)) map1.put(key, new HashSet<>());
      Set<String> set1 = map1.get(key);
      List<String> set2 = entry2.getValue();

      for (String string : set2) {
        if (string.startsWith("-")) {
          set1.remove(string.substring(1));
        } else {
          set1.add(string);
        }
      }
    }
  }

  private static <T> boolean mapOfSetsEqual(@Nullable Map<String, Set<T>> map1,
        @Nullable Map<String, Set<T>> map2, @Nullable String key) {
    if (key == null) return true;

    if ((map1 != null) && (map2 != null)) {
      @Nullable Set<T> set1 = map1.get(key);
      @Nullable Set<T> set2 = map2.get(key);
      return ((set1 != null) ? set1.equals(set2) : (set2 == null));
    } else {
      return ((map1 != null) ? (map2 != null) : (map2 == null));
    }
  }

  private static <T> int mapOfSetsHashCode(
        @Nullable Map<String, Set<T>> map, @Nullable String key) {
    return (((map != null) && (key != null) && map.containsKey(key)) ? map.get(key).hashCode() : 0);
  }

  private void setSettings(@UnknownInitialization Settings this,
        JsonElement jsonSettings, @Nullable JsonElement jsonWorkspaceSpecificSettings) {
    if (jsonWorkspaceSpecificSettings == null) jsonWorkspaceSpecificSettings = jsonSettings;

    try {
      JsonElement jsonElement = getSettingFromJson(jsonSettings, "enabled");

      if (jsonElement.isJsonArray()) {
        this.enabled = convertJsonArrayToSet(jsonElement.getAsJsonArray());
      } else {
        this.enabled = (jsonElement.getAsBoolean() ? defaultEnabled : Collections.emptySet());
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.enabled = null;
    }

    try {
      this.languageShortCode = getSettingFromJson(jsonSettings, "language").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.languageShortCode = null;
    }

    this.dictionary = new HashMap<>();
    this.disabledRules = new HashMap<>();
    this.enabledRules = new HashMap<>();
    this.hiddenFalsePositives = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, Set<String>> dictionary = this.dictionary;
    Map<String, Set<String>> disabledRules = this.disabledRules;
    Map<String, Set<String>> enabledRules = this.enabledRules;
    Map<String, Set<HiddenFalsePositive>> hiddenFalsePositives = this.hiddenFalsePositives;

    try {
      mergeMapOfListsIntoMapOfSets(dictionary, convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonWorkspaceSpecificSettings, "dictionary").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      mergeMapOfListsIntoMapOfSets(disabledRules, convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonWorkspaceSpecificSettings, "disabledRules").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      mergeMapOfListsIntoMapOfSets(enabledRules, convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonWorkspaceSpecificSettings, "enabledRules").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      Map<String, Set<String>> hiddenFalsePositiveJsonStrings = new HashMap<>();
      mergeMapOfListsIntoMapOfSets(hiddenFalsePositiveJsonStrings, convertJsonObjectToMapOfLists(
          getSettingFromJson(jsonWorkspaceSpecificSettings,
          "hiddenFalsePositives").getAsJsonObject()));

      for (Map.Entry<String, Set<String>> entry : hiddenFalsePositiveJsonStrings.entrySet()) {
        String curLanguage = entry.getKey();
        @Nullable Set<HiddenFalsePositive> curHiddenFalsePositives =
            hiddenFalsePositives.get(curLanguage);

        if (curHiddenFalsePositives == null) {
          curHiddenFalsePositives = new HashSet<>();
          hiddenFalsePositives.put(curLanguage, curHiddenFalsePositives);
        }

        for (String hiddenFalsePositiveJsonString : entry.getValue()) {
          curHiddenFalsePositives.add(
              HiddenFalsePositive.fromJsonString(hiddenFalsePositiveJsonString));
        }
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    this.bibtexFields = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, Boolean> bibtexFields = this.bibtexFields;

    try {
      bibtexFields.putAll(convertJsonObjectToMapOfBooleans(
          getSettingFromJson(jsonSettings, "bibtex.fields").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    this.latexCommands = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, String> latexCommands = this.latexCommands;

    try {
      latexCommands.putAll(convertJsonObjectToMapOfStrings(
          getSettingFromJson(jsonSettings, "latex.commands").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    this.latexEnvironments = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, String> latexEnvironments = this.latexEnvironments;

    try {
      latexEnvironments.putAll(convertJsonObjectToMapOfStrings(
          getSettingFromJson(jsonSettings, "latex.environments").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    this.markdownNodes = new HashMap<>();

    // fixes false-positive argument.type.incompatible warnings
    Map<String, String> markdownNodes = this.markdownNodes;

    try {
      markdownNodes.putAll(convertJsonObjectToMapOfStrings(
          getSettingFromJson(jsonSettings, "markdown.nodes").getAsJsonObject()));
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      // setting not set
    }

    try {
      this.enablePickyRules = getSettingFromJson(
          jsonSettings, "additionalRules.enablePickyRules").getAsBoolean();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.enablePickyRules = null;
    }

    try {
      this.motherTongueShortCode = getSettingFromJson(
          jsonSettings, "additionalRules.motherTongue").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.motherTongueShortCode = null;
    }

    try {
      this.languageModelRulesDirectory = getSettingFromJson(
          jsonSettings, "additionalRules.languageModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.languageModelRulesDirectory = null;
    }

    try {
      this.neuralNetworkModelRulesDirectory = getSettingFromJson(
          jsonSettings, "additionalRules.neuralNetworkModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.neuralNetworkModelRulesDirectory = null;
    }

    try {
      this.word2VecModelRulesDirectory = getSettingFromJson(
          jsonSettings, "additionalRules.word2VecModel").getAsString();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.word2VecModelRulesDirectory = null;
    }

    try {
      this.languageToolHttpServerUri = getSettingFromJson(
          jsonSettings, "ltex-ls.languageToolHttpServerUri").getAsString();
    } catch (NullPointerException | UnsupportedOperationException e) {
      this.languageToolHttpServerUri = null;
    }

    try {
      this.logLevel = Level.parse(
          getSettingFromJson(jsonSettings, "ltex-ls.logLevel").getAsString().toUpperCase());
    } catch (NullPointerException | UnsupportedOperationException | IllegalArgumentException e) {
      this.logLevel = null;
    }

    try {
      this.sentenceCacheSize = getSettingFromJson(
          jsonSettings, "sentenceCacheSize").getAsInt();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.sentenceCacheSize = null;
    }

    try {
      String diagnosticSeverityString =
          getSettingFromJson(jsonSettings, "diagnosticSeverity").getAsString();

      if (diagnosticSeverityString.equals("error")) {
        this.diagnosticSeverity = DiagnosticSeverity.Error;
      } else if (diagnosticSeverityString.equals("warning")) {
        this.diagnosticSeverity = DiagnosticSeverity.Warning;
      } else if (diagnosticSeverityString.equals("information")) {
        this.diagnosticSeverity = DiagnosticSeverity.Information;
      } else if (diagnosticSeverityString.equals("hint")) {
        this.diagnosticSeverity = DiagnosticSeverity.Hint;
      } else {
        this.diagnosticSeverity = null;
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.diagnosticSeverity = null;
    }

    try {
      String checkFrequencyString =
          getSettingFromJson(jsonSettings, "checkFrequency").getAsString();

      if (checkFrequencyString.equals("edit")) {
        this.checkFrequency = CheckFrequency.EDIT;
      } else if (checkFrequencyString.equals("save")) {
        this.checkFrequency = CheckFrequency.SAVE;
      } else if (checkFrequencyString.equals("manual")) {
        this.checkFrequency = CheckFrequency.MANUAL;
      } else {
        this.checkFrequency = null;
      }
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.checkFrequency = null;
    }

    try {
      this.clearDiagnosticsWhenClosingFile = getSettingFromJson(
          jsonSettings, "clearDiagnosticsWhenClosingFile").getAsBoolean();
    } catch (NullPointerException | UnsupportedOperationException | IllegalStateException e) {
      this.clearDiagnosticsWhenClosingFile = null;
    }
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if ((obj == null) || !Settings.class.isAssignableFrom(obj.getClass())) return false;
    Settings other = (Settings)obj;

    if (!Tools.equals(this.enabled, other.enabled)) return false;
    if (!Tools.equals(this.languageShortCode, other.languageShortCode)) return false;

    if (!mapOfSetsEqual(this.dictionary, other.dictionary, this.languageShortCode)) {
      return false;
    }

    if (!mapOfSetsEqual(this.disabledRules, other.disabledRules, this.languageShortCode)) {
      return false;
    }

    if (!mapOfSetsEqual(this.enabledRules, other.enabledRules, this.languageShortCode)) {
      return false;
    }

    if (!mapOfSetsEqual(this.hiddenFalsePositives, other.hiddenFalsePositives,
          this.languageShortCode)) {
      return false;
    }

    if (!Tools.equals(this.bibtexFields, other.bibtexFields)) return false;
    if (!Tools.equals(this.latexCommands, other.latexCommands)) return false;
    if (!Tools.equals(this.latexEnvironments, other.latexEnvironments)) return false;
    if (!Tools.equals(this.markdownNodes, other.markdownNodes)) return false;
    if (!Tools.equals(this.enablePickyRules, other.enablePickyRules)) return false;
    if (!Tools.equals(this.motherTongueShortCode, other.motherTongueShortCode)) return false;

    if (!Tools.equals(this.languageModelRulesDirectory, other.languageModelRulesDirectory)) {
      return false;
    }

    if (!Tools.equals(this.neuralNetworkModelRulesDirectory,
          other.neuralNetworkModelRulesDirectory)) {
      return false;
    }

    if (!Tools.equals(this.word2VecModelRulesDirectory, other.word2VecModelRulesDirectory)) {
      return false;
    }

    if (!Tools.equals(this.languageToolHttpServerUri, other.languageToolHttpServerUri)) {
      return false;
    }

    if (!Tools.equals(this.logLevel, other.logLevel)) return false;
    if (!Tools.equals(this.sentenceCacheSize, other.sentenceCacheSize)) return false;
    if (!Tools.equals(this.diagnosticSeverity, other.diagnosticSeverity)) return false;
    if (!Tools.equals(this.checkFrequency, other.checkFrequency)) return false;

    if (!Tools.equals(this.clearDiagnosticsWhenClosingFile,
          other.clearDiagnosticsWhenClosingFile)) {
      return false;
    }

    return true;
  }

  public Set<SettingsDifference> getDifferencesRelevantForLanguageTool(@Nullable Settings other) {
    Set<SettingsDifference> differences = new HashSet<>();

    if (other == null) {
      differences.add(new SettingsDifference("settings", "non-null", "null"));
      return differences;
    }

    if (!mapOfSetsEqual(this.dictionary, other.dictionary, this.languageShortCode)) {
      differences.add(new SettingsDifference("dictionary", this.dictionary, other.dictionary));
    }

    if (!mapOfSetsEqual(this.disabledRules, other.disabledRules, this.languageShortCode)) {
      differences.add(new SettingsDifference("disabledRules",
          this.disabledRules, other.disabledRules));
    }

    if (!mapOfSetsEqual(this.enabledRules, other.enabledRules, this.languageShortCode)) {
      differences.add(new SettingsDifference("enabledRules",
          this.enabledRules, other.enabledRules));
    }


    if (!Tools.equals(this.motherTongueShortCode, other.motherTongueShortCode)) {
      differences.add(new SettingsDifference("additionalRules.motherTongue",
          this.motherTongueShortCode, other.motherTongueShortCode));
    }

    if (!Tools.equals(this.languageModelRulesDirectory, other.languageModelRulesDirectory)) {
      differences.add(new SettingsDifference("additionalRules.languageModel",
          this.languageModelRulesDirectory, other.languageModelRulesDirectory));
    }

    if (!Tools.equals(this.neuralNetworkModelRulesDirectory,
          other.neuralNetworkModelRulesDirectory)) {
      differences.add(new SettingsDifference("additionalRules.neuralNetworkModel",
          this.neuralNetworkModelRulesDirectory, other.neuralNetworkModelRulesDirectory));
    }

    if (!Tools.equals(this.word2VecModelRulesDirectory, other.word2VecModelRulesDirectory)) {
      differences.add(new SettingsDifference("additionalRules.word2VecModel",
          this.word2VecModelRulesDirectory, other.word2VecModelRulesDirectory));
    }

    if (!Tools.equals(this.languageToolHttpServerUri, other.languageToolHttpServerUri)) {
      differences.add(new SettingsDifference("ltex-ls.languageToolHttpServerUri",
          this.languageToolHttpServerUri, other.languageToolHttpServerUri));
    }

    if (!Tools.equals(this.sentenceCacheSize, other.sentenceCacheSize)) {
      differences.add(new SettingsDifference("sentenceCacheSize",
          this.sentenceCacheSize, other.sentenceCacheSize));
    }

    return differences;
  }

  @Override
  public int hashCode() {
    int hash = 3;

    hash = 53 * hash + ((this.enabled != null) ? this.enabled.hashCode() : 0);
    hash = 53 * hash + ((this.languageShortCode != null) ? this.languageShortCode.hashCode() : 0);
    hash = 53 * hash + mapOfSetsHashCode(this.dictionary, this.languageShortCode);
    hash = 53 * hash + mapOfSetsHashCode(this.disabledRules, this.languageShortCode);
    hash = 53 * hash + mapOfSetsHashCode(this.enabledRules, this.languageShortCode);
    hash = 53 * hash + mapOfSetsHashCode(this.hiddenFalsePositives, this.languageShortCode);
    hash = 53 * hash + ((this.bibtexFields != null) ? this.bibtexFields.hashCode() : 0);
    hash = 53 * hash + ((this.latexCommands != null) ? this.latexCommands.hashCode() : 0);
    hash = 53 * hash + ((this.latexEnvironments != null) ? this.latexEnvironments.hashCode() : 0);
    hash = 53 * hash + ((this.markdownNodes != null) ? this.markdownNodes.hashCode() : 0);
    hash = 53 * hash + ((this.enablePickyRules != null) ? this.enablePickyRules.hashCode() : 0);
    hash = 53 * hash + ((this.motherTongueShortCode != null)
        ? this.motherTongueShortCode.hashCode() : 0);
    hash = 53 * hash + ((this.languageModelRulesDirectory != null)
        ? this.languageModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((this.neuralNetworkModelRulesDirectory != null)
        ? this.neuralNetworkModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((this.word2VecModelRulesDirectory != null)
        ? this.word2VecModelRulesDirectory.hashCode() : 0);
    hash = 53 * hash + ((this.languageToolHttpServerUri != null)
        ? this.languageToolHttpServerUri.hashCode() : 0);
    hash = 53 * hash + ((this.logLevel != null) ? this.logLevel.hashCode() : 0);
    hash = 53 * hash + ((this.sentenceCacheSize != null)
        ? this.sentenceCacheSize.hashCode() : 0);
    hash = 53 * hash + ((this.diagnosticSeverity != null) ? this.diagnosticSeverity.hashCode() : 0);
    hash = 53 * hash + ((this.checkFrequency != null) ? this.checkFrequency.hashCode() : 0);
    hash = 53 * hash + ((this.clearDiagnosticsWhenClosingFile != null)
        ? this.clearDiagnosticsWhenClosingFile.hashCode() : 0);

    return hash;
  }

  private static <T> T getDefault(@Nullable T obj, T defaultValue) {
    return ((obj != null) ? obj : defaultValue);
  }

  private static <T> Set<T> getDefault(@Nullable Map<String, Set<T>> map, String key,
        Set<T> defaultValue) {
    return (((map != null) && map.containsKey(key)) ? map.get(key) : defaultValue);
  }

  public Set<String> getEnabled() {
    return getDefault(this.enabled, defaultEnabled);
  }

  public String getLanguageShortCode() {
    return getDefault(this.languageShortCode, "en-US");
  }

  public Set<String> getDictionary() {
    return Collections.unmodifiableSet(getDefault(
        this.dictionary, getLanguageShortCode(), Collections.emptySet()));
  }

  public Set<String> getDisabledRules() {
    return Collections.unmodifiableSet(getDefault(
        this.disabledRules, getLanguageShortCode(), Collections.emptySet()));
  }

  public Set<String> getEnabledRules() {
    return Collections.unmodifiableSet(getDefault(
        this.enabledRules, getLanguageShortCode(), Collections.emptySet()));
  }

  public Set<HiddenFalsePositive> getHiddenFalsePositives() {
    return Collections.unmodifiableSet(getDefault(
        this.hiddenFalsePositives, getLanguageShortCode(), Collections.emptySet()));
  }

  public Map<String, Boolean> getBibtexFields() {
    return Collections.unmodifiableMap(
        getDefault(this.bibtexFields, Collections.emptyMap()));
  }

  public Map<String, String> getLatexCommands() {
    return Collections.unmodifiableMap(
        getDefault(this.latexCommands, Collections.emptyMap()));
  }

  public Map<String, String> getLatexEnvironments() {
    return Collections.unmodifiableMap(
        getDefault(this.latexEnvironments, Collections.emptyMap()));
  }

  public Map<String, String> getMarkdownNodes() {
    return Collections.unmodifiableMap(
        getDefault(this.markdownNodes, Collections.emptyMap()));
  }

  public Boolean getEnablePickyRules() {
    return getDefault(this.enablePickyRules, false);
  }

  public String getMotherTongueShortCode() {
    return getDefault(this.motherTongueShortCode, "");
  }

  public String getLanguageModelRulesDirectory() {
    return Tools.normalizePath(getDefault(this.languageModelRulesDirectory, ""));
  }

  public String getNeuralNetworkModelRulesDirectory() {
    return Tools.normalizePath(getDefault(this.neuralNetworkModelRulesDirectory, ""));
  }

  public String getWord2VecModelRulesDirectory() {
    return Tools.normalizePath(getDefault(this.word2VecModelRulesDirectory, ""));
  }

  public String getLanguageToolHttpServerUri() {
    return getDefault(this.languageToolHttpServerUri, "");
  }

  public Level getLogLevel() {
    return getDefault(this.logLevel, Level.FINE);
  }

  public Integer getSentenceCacheSize() {
    return getDefault(this.sentenceCacheSize, 2000);
  }

  public DiagnosticSeverity getDiagnosticSeverity() {
    return getDefault(this.diagnosticSeverity, DiagnosticSeverity.Information);
  }

  public CheckFrequency getCheckFrequency() {
    return getDefault(this.checkFrequency, CheckFrequency.EDIT);
  }

  public Boolean getClearDiagnosticsWhenClosingFile() {
    return getDefault(this.clearDiagnosticsWhenClosingFile, true);
  }

  public Settings withEnabled(Set<String> enabled) {
    Settings obj = new Settings(this);
    obj.enabled = enabled;
    return obj;
  }

  public Settings withEnabled(boolean enabled) {
    return withEnabled(enabled ? defaultEnabled : Collections.emptySet());
  }

  public Settings withLanguageShortCode(String languageShortCode) {
    Settings obj = new Settings(this);
    obj.languageShortCode = languageShortCode;
    return obj;
  }

  public Settings withDictionary(Set<String> dictionary) {
    Settings obj = new Settings(this);
    if (obj.dictionary == null) obj.dictionary = new HashMap<>();
    obj.dictionary.put(getLanguageShortCode(), new HashSet<>(dictionary));
    return obj;
  }

  public Settings withDisabledRules(Set<String> disabledRules) {
    Settings obj = new Settings(this);
    if (obj.disabledRules == null) obj.disabledRules = new HashMap<>();
    obj.disabledRules.put(getLanguageShortCode(), new HashSet<>(disabledRules));
    return obj;
  }

  public Settings withEnabledRules(Set<String> enabledRules) {
    Settings obj = new Settings(this);
    if (obj.enabledRules == null) obj.enabledRules = new HashMap<>();
    obj.enabledRules.put(getLanguageShortCode(), new HashSet<>(enabledRules));
    return obj;
  }

  public Settings withHiddenFalsePositives(Set<HiddenFalsePositive> hiddenFalsePositives) {
    Settings obj = new Settings(this);
    if (obj.hiddenFalsePositives == null) obj.hiddenFalsePositives = new HashMap<>();
    obj.hiddenFalsePositives.put(getLanguageShortCode(), new HashSet<>(hiddenFalsePositives));
    return obj;
  }

  public Settings withBibtexFields(Map<String, Boolean> bibtexFields) {
    Settings obj = new Settings(this);
    obj.bibtexFields = new HashMap<>(bibtexFields);
    return obj;
  }

  public Settings withLatexCommands(Map<String, String> latexCommands) {
    Settings obj = new Settings(this);
    obj.latexCommands = new HashMap<>(latexCommands);
    return obj;
  }

  public Settings withLatexEnvironments(Map<String, String> latexEnvironments) {
    Settings obj = new Settings(this);
    obj.latexEnvironments = new HashMap<>(latexEnvironments);
    return obj;
  }

  public Settings withMarkdownNodes(Map<String, String> markdownNodes) {
    Settings obj = new Settings(this);
    obj.markdownNodes = new HashMap<>(markdownNodes);
    return obj;
  }

  public Settings withEnablePickyRules(Boolean enablePickyRules) {
    Settings obj = new Settings(this);
    obj.enablePickyRules = enablePickyRules;
    return obj;
  }

  public Settings withMotherTongueShortCode(String motherTongueShortCode) {
    Settings obj = new Settings(this);
    obj.motherTongueShortCode = motherTongueShortCode;
    return obj;
  }

  public Settings withLanguageModelRulesDirectory(String languageModelRulesDirectory) {
    Settings obj = new Settings(this);
    obj.languageModelRulesDirectory = languageModelRulesDirectory;
    return obj;
  }

  public Settings withNeuralNetworkModelRulesDirectory(String neuralNetworkModelRulesDirectory) {
    Settings obj = new Settings(this);
    obj.neuralNetworkModelRulesDirectory = neuralNetworkModelRulesDirectory;
    return obj;
  }

  public Settings withWord2VecModelRulesDirectory(String word2VecModelRulesDirectory) {
    Settings obj = new Settings(this);
    obj.word2VecModelRulesDirectory = word2VecModelRulesDirectory;
    return obj;
  }

  public Settings withLanguageToolHttpServerUri(String languageToolHttpServerUri) {
    Settings obj = new Settings(this);
    obj.languageToolHttpServerUri = languageToolHttpServerUri;
    return obj;
  }

  public Settings withLogLevel(Level logLevel) {
    Settings obj = new Settings(this);
    obj.logLevel = logLevel;
    return obj;
  }

  public Settings withSentenceCacheSize(Integer sentenceCacheSize) {
    Settings obj = new Settings(this);
    obj.sentenceCacheSize = sentenceCacheSize;
    return obj;
  }

  public Settings withDiagnosticSeverity(DiagnosticSeverity diagnosticSeverity) {
    Settings obj = new Settings(this);
    obj.diagnosticSeverity = diagnosticSeverity;
    return obj;
  }

  public Settings withCheckFrequency(CheckFrequency checkFrequency) {
    Settings obj = new Settings(this);
    obj.checkFrequency = checkFrequency;
    return obj;
  }

  public Settings withClearDiagnosticsWhenClosingFile(Boolean clearDiagnosticsWhenClosingFile) {
    Settings obj = new Settings(this);
    obj.clearDiagnosticsWhenClosingFile = clearDiagnosticsWhenClosingFile;
    return obj;
  }
}
