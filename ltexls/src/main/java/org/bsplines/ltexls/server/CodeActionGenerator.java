/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bsplines.ltexls.languagetool.LanguageToolRuleMatch;
import org.bsplines.ltexls.parsing.AnnotatedTextFragment;
import org.bsplines.ltexls.parsing.CodeFragment;
import org.bsplines.ltexls.settings.SettingsManager;
import org.bsplines.ltexls.tools.Tools;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.xbase.lib.Pair;

public class CodeActionGenerator {
  private SettingsManager settingsManager;

  private static final String acceptSuggestionsCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.acceptSuggestions";
  private static final String addToDictionaryCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.addToDictionary";
  private static final String disableRulesCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.disableRules";
  private static final String hideFalsePositivesCodeActionKind =
      CodeActionKind.QuickFix + ".ltex.hideFalsePositives";
  private static final String addToDictionaryCommandName = "_ltex.addToDictionary";
  private static final String disableRulesCommandName = "_ltex.disableRules";
  private static final String hideFalsePositivesCommandName = "_ltex.hideFalsePositives";
  private static final String dummyPatternStr = "(?:Dummy|Ina|Jimmy-)[0-9]+";
  private static final Pattern dummyPattern = Pattern.compile(dummyPatternStr);

  private static final int maximumNumberOfAcceptSuggestionsCodeActions = 5;

  public CodeActionGenerator(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  public Diagnostic createDiagnostic(LanguageToolRuleMatch match, LtexTextDocumentItem document) {
    Diagnostic ret = new Diagnostic();
    ret.setRange(new Range(document.convertPosition(match.getFromPos()),
        document.convertPosition(match.getToPos())));
    ret.setSeverity(this.settingsManager.getSettings().getDiagnosticSeverity());
    ret.setSource("LTeX");
    ret.setMessage(match.getMessage().replaceAll("<suggestion>(.*?)</suggestion>", "'$1'")
        + " \u2013 " + match.getRuleId());
    return ret;
  }

  private static int findAnnotatedTextFragmentWithMatch(
        List<AnnotatedTextFragment> annotatedTextFragments, LanguageToolRuleMatch match) {
    for (int i = 0; i < annotatedTextFragments.size(); i++) {
      if (annotatedTextFragments.get(i).getCodeFragment().contains(match)) return i;
    }

    return -1;
  }

  private static void addToMap(String key, String value,
        Map<String, List<String>> map, JsonObject jsonObject) {
    if (!map.containsKey(key)) {
      map.put(key, new ArrayList<>());
      jsonObject.add(key, new JsonArray());
    }

    List<String> unknownWordsList = map.get(key);
    JsonArray unknownWordsJsonArray = jsonObject.getAsJsonArray(key);

    if (!unknownWordsList.contains(value)) {
      unknownWordsList.add(value);
      unknownWordsJsonArray.add(value);
    }
  }

  private static @Nullable String getOnlyEntry(Map<String, List<String>> map) {
    if (map.size() == 1) {
      List<String> list = map.entrySet().stream().findFirst().get().getValue();
      if (list.size() == 1) return list.get(0);
    }

    return null;
  }

  public List<Either<Command, CodeAction>> generate(
        CodeActionParams params, LtexTextDocumentItem document,
        Pair<List<LanguageToolRuleMatch>, List<AnnotatedTextFragment>> checkingResult) {
    if (checkingResult.getValue() == null) return Collections.emptyList();

    List<AnnotatedTextFragment> annotatedTextFragments = checkingResult.getValue();
    List<Either<Command, CodeAction>> result =
        new ArrayList<Either<Command, CodeAction>>();

    Map<String, List<LanguageToolRuleMatch>> acceptSuggestionsMatchesMap = new LinkedHashMap<>();
    List<LanguageToolRuleMatch> addToDictionaryMatches = new ArrayList<>();
    List<LanguageToolRuleMatch> hideFalsePositivesMatches = new ArrayList<>();
    List<LanguageToolRuleMatch> disableRulesMatches = new ArrayList<>();

    for (LanguageToolRuleMatch match : checkingResult.getKey()) {
      if (match.isIntersectingWithRange(params.getRange(), document)) {
        for (String newWord : match.getSuggestedReplacements()) {
          if (!acceptSuggestionsMatchesMap.containsKey(newWord)) {
            if (acceptSuggestionsMatchesMap.size() >= maximumNumberOfAcceptSuggestionsCodeActions) {
              continue;
            }

            acceptSuggestionsMatchesMap.put(newWord, new ArrayList<>());
          }

          acceptSuggestionsMatchesMap.get(newWord).add(match);
        }

        if (match.isUnknownWordRule()) addToDictionaryMatches.add(match);
        if (match.getSentence() != null) hideFalsePositivesMatches.add(match);
        disableRulesMatches.add(match);
      }
    }

    for (Map.Entry<String, List<LanguageToolRuleMatch>> entry
          : acceptSuggestionsMatchesMap.entrySet()) {
      result.add(Either.forRight(getAcceptSuggestionsCodeAction(
          document, entry.getKey(), entry.getValue())));
    }

    if (!addToDictionaryMatches.isEmpty()
          && this.settingsManager.getSettings().getLanguageToolHttpServerUri().isEmpty()) {
      result.add(Either.forRight(getAddWordToDictionaryCodeAction(document,
          addToDictionaryMatches, annotatedTextFragments)));
    }

    if (!hideFalsePositivesMatches.isEmpty()) {
      result.add(Either.forRight(getHideFalsePositivesCodeAction(document,
          hideFalsePositivesMatches, annotatedTextFragments)));
    }

    if (!disableRulesMatches.isEmpty()) {
      result.add(Either.forRight(getDisableRulesCodeAction(document,
          disableRulesMatches, annotatedTextFragments)));
    }

    return result;
  }

  private CodeAction getAcceptSuggestionsCodeAction(LtexTextDocumentItem document, String newWord,
        List<LanguageToolRuleMatch> acceptSuggestionsMatches) {
    VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(
        document.getUri(), document.getVersion());
    List<Diagnostic> diagnostics = new ArrayList<>();
    List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();

    for (LanguageToolRuleMatch match : acceptSuggestionsMatches) {
      Diagnostic diagnostic = createDiagnostic(match, document);
      Range range = diagnostic.getRange();

      diagnostics.add(diagnostic);
      documentChanges.add(Either.forLeft(new TextDocumentEdit(textDocument,
          Collections.singletonList(new TextEdit(range, newWord)))));
    }

    CodeAction codeAction = new CodeAction((acceptSuggestionsMatches.size() == 1)
        ? Tools.i18n("useWord", newWord) : Tools.i18n("useWordAllSelectedMatches", newWord));
    codeAction.setKind(acceptSuggestionsCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setEdit(new WorkspaceEdit(documentChanges));

    return codeAction;
  }

  private CodeAction getAddWordToDictionaryCodeAction(
        LtexTextDocumentItem document,
        List<LanguageToolRuleMatch> addToDictionaryMatches,
        List<AnnotatedTextFragment> annotatedTextFragments) {
    Map<String, List<String>> unknownWordsMap = new HashMap<>();
    JsonObject unknownWordsJsonObject = new JsonObject();
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (LanguageToolRuleMatch match : addToDictionaryMatches) {
      int fragmentIndex = findAnnotatedTextFragmentWithMatch(
          annotatedTextFragments, match);

      if (fragmentIndex == -1) {
        Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
        continue;
      }

      AnnotatedTextFragment annotatedTextFragment = annotatedTextFragments.get(fragmentIndex);
      CodeFragment codeFragment = annotatedTextFragment.getCodeFragment();
      String language = codeFragment.getSettings().getLanguageShortCode();
      int offset = codeFragment.getFromPos();
      String word = annotatedTextFragment.getSubstringOfPlainText(
          match.getFromPos() - offset, match.getToPos() - offset);

      addToMap(language, word, unknownWordsMap, unknownWordsJsonObject);
      diagnostics.add(createDiagnostic(match, document));
    }

    JsonObject arguments = new JsonObject();
    arguments.addProperty("uri", document.getUri());
    arguments.add("words", unknownWordsJsonObject);

    @Nullable String onlyUnknownWord = getOnlyEntry(unknownWordsMap);
    String commandTitle = ((onlyUnknownWord != null)
        ? Tools.i18n("addWordToDictionary", onlyUnknownWord)
        : Tools.i18n("addAllUnknownWordsInSelectionToDictionary"));
    Command command = new Command(commandTitle, addToDictionaryCommandName);
    command.setArguments(Collections.singletonList(arguments));

    CodeAction codeAction = new CodeAction(command.getTitle());
    codeAction.setKind(addToDictionaryCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setCommand(command);

    return codeAction;
  }

  private CodeAction getHideFalsePositivesCodeAction(
        LtexTextDocumentItem document,
        List<LanguageToolRuleMatch> hideFalsePositivesMatches,
        List<AnnotatedTextFragment> annotatedTextFragments) {
    List<Pair<String, String>> ruleIdSentencePairs = new ArrayList<>();
    Map<String, List<String>> hiddenFalsePositivesMap = new HashMap<>();
    JsonObject falsePositivesJsonObject = new JsonObject();
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (LanguageToolRuleMatch match : hideFalsePositivesMatches) {
      @Nullable String ruleId = match.getRuleId();
      @Nullable String sentence = match.getSentence();
      if ((ruleId == null) || (sentence == null)) continue;
      sentence = sentence.trim();
      Pair<String, String> pair = new Pair<>(ruleId, sentence);

      if (!ruleIdSentencePairs.contains(pair)) {
        int fragmentIndex = findAnnotatedTextFragmentWithMatch(
            annotatedTextFragments, match);

        if (fragmentIndex == -1) {
          Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
          continue;
        }

        AnnotatedTextFragment annotatedTextFragment = annotatedTextFragments.get(fragmentIndex);
        CodeFragment codeFragment = annotatedTextFragment.getCodeFragment();
        final String language = codeFragment.getSettings().getLanguageShortCode();

        Matcher matcher = dummyPattern.matcher(sentence);
        StringBuilder sentencePatternStringBuilder = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
          sentencePatternStringBuilder.append(Pattern.quote(
              sentence.substring(lastEnd, matcher.start())));
          sentencePatternStringBuilder.append(dummyPatternStr);
          lastEnd = matcher.end();
        }

        if (lastEnd < sentence.length()) {
          sentencePatternStringBuilder.append(Pattern.quote(sentence.substring(lastEnd)));
        }

        ruleIdSentencePairs.add(pair);

        JsonObject falsePositiveJson = new JsonObject();
        falsePositiveJson.add("rule", new JsonPrimitive(ruleId));
        String sentencePatternString = "^" + sentencePatternStringBuilder.toString() + "$";
        falsePositiveJson.add("sentence", new JsonPrimitive(sentencePatternString));

        addToMap(language, falsePositiveJson.toString(), hiddenFalsePositivesMap,
            falsePositivesJsonObject);
      }

      diagnostics.add(createDiagnostic(match, document));
    }

    JsonObject arguments = new JsonObject();
    arguments.addProperty("uri", document.getUri());
    arguments.add("falsePositives", falsePositivesJsonObject);

    Command command = new Command(((ruleIdSentencePairs.size() == 1)
        ? Tools.i18n("hideFalsePositive")
        : Tools.i18n("hideAllFalsePositivesInTheSelectedSentences")),
        hideFalsePositivesCommandName);
    command.setArguments(Collections.singletonList(arguments));

    CodeAction codeAction = new CodeAction(command.getTitle());
    codeAction.setKind(hideFalsePositivesCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setCommand(command);

    return codeAction;
  }

  private CodeAction getDisableRulesCodeAction(
        LtexTextDocumentItem document,
        List<LanguageToolRuleMatch> disableRuleMatches,
        List<AnnotatedTextFragment> annotatedTextFragments) {
    Map<String, List<String>> ruleIdsMap = new HashMap<>();
    JsonObject ruleIdsJsonObject = new JsonObject();
    List<Diagnostic> diagnostics = new ArrayList<>();

    for (LanguageToolRuleMatch match : disableRuleMatches) {
      @Nullable String ruleId = match.getRuleId();

      if (ruleId != null) {
        int fragmentIndex = findAnnotatedTextFragmentWithMatch(
            annotatedTextFragments, match);

        if (fragmentIndex == -1) {
          Tools.logger.warning(Tools.i18n("couldNotFindFragmentForMatch"));
          continue;
        }

        AnnotatedTextFragment annotatedTextFragment = annotatedTextFragments.get(fragmentIndex);
        String language =
            annotatedTextFragment.getCodeFragment().getSettings().getLanguageShortCode();
        addToMap(language, ruleId, ruleIdsMap, ruleIdsJsonObject);
      }

      diagnostics.add(createDiagnostic(match, document));
    }

    JsonObject arguments = new JsonObject();
    arguments.addProperty("uri", document.getUri());
    arguments.add("ruleIds", ruleIdsJsonObject);

    String commandTitle = ((getOnlyEntry(ruleIdsMap) != null)
        ? Tools.i18n("disableRule")
        : Tools.i18n("disableAllRulesWithMatchesInSelection"));
    Command command = new Command(commandTitle, disableRulesCommandName);
    command.setArguments(Collections.singletonList(arguments));

    CodeAction codeAction = new CodeAction(command.getTitle());
    codeAction.setKind(disableRulesCodeActionKind);
    codeAction.setDiagnostics(diagnostics);
    codeAction.setCommand(command);

    return codeAction;
  }

  public static List<String> getCodeActions() {
    return Collections.singletonList(acceptSuggestionsCodeActionKind);
  }
}
