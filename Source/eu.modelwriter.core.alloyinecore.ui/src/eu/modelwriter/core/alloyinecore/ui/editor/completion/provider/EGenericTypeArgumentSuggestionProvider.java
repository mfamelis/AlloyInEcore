package eu.modelwriter.core.alloyinecore.ui.editor.completion.provider;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EGenericTypeArgumentContext;
import eu.modelwriter.core.alloyinecore.ui.editor.completion.util.AbstractAIESuggestionProvider;

public class EGenericTypeArgumentSuggestionProvider extends AbstractAIESuggestionProvider {

  @Override
  public Set<ICompletionProposal> getStartSuggestions() {
    final Set<ICompletionProposal> startSuggestions = new HashSet<>();
    startSuggestions.addAll(
        spFactory.eGenericTypeSP().getStartSuggestions());
    startSuggestions.addAll(spFactory.eGenericWildcardSP()
        .getStartSuggestions());
    return startSuggestions;
  }

  @Override
  protected void initParentProviders() {
    parents.add(spFactory.eGenericTypeSP());
  }

  @Override
  protected void initChildProviders() {
    children.add(spFactory.eGenericTypeSP());
    children.add(spFactory.eGenericWildcardSP());
  }

  @Override
  protected void computeSuggestions(final ParserRuleContext context, final ParseTree lastToken) {
    suggestions.addAll(getParentProviderSuggestions(context, lastToken));
  }

  @Override
  protected boolean isCompatibleWithContext(final ParserRuleContext context) {
    return context instanceof EGenericTypeArgumentContext;
  }

}
