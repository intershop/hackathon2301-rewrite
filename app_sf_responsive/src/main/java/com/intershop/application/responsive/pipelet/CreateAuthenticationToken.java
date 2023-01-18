package com.intershop.application.responsive.pipelet;

import javax.inject.Inject;
import javax.inject.Provider;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.user.User;
import com.intershop.component.rest.capi.auth.Token;
import com.intershop.component.rest.capi.auth.TokenFactory;


/**
 * Creates a token that can be used for authentication.
 */
public class CreateAuthenticationToken extends Pipelet
{

    @Inject
    private Provider<TokenFactory> tokenFactoryProvider;

    
    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        User user = aPipelineDictionary.getRequired("User");
        Token token = tokenFactoryProvider.get().create(user);
        aPipelineDictionary.put("Token", token);
        return PIPELET_NEXT;
    }

}
