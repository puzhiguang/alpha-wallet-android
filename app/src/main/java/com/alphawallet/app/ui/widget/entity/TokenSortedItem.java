package com.alphawallet.app.ui.widget.entity;

import com.alphawallet.app.entity.TokenMeta;
import com.alphawallet.app.ui.widget.holder.TokenHolder;
import com.alphawallet.app.entity.Ticket;
import com.alphawallet.app.entity.Token;

public class TokenSortedItem extends SortedItem<TokenMeta> {

    public TokenSortedItem(TokenMeta value, int weight) {
        super(TokenHolder.VIEW_TYPE, value, weight);
    }

    @Override
    public int compare(SortedItem other) {
        return weight - other.weight;
    }

    @Override
    public boolean areContentsTheSame(SortedItem newItem)
    {
        if (viewType == newItem.viewType)
        {
            TokenMeta oldToken = value;
            TokenMeta newToken = (TokenMeta) newItem.value;
            return !oldToken.update || !newToken.update;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean areItemsTheSame(SortedItem other)
    {
        if (viewType == other.viewType)
        {
            TokenMeta oldToken = value;
            TokenMeta newToken = (TokenMeta) other.value;

            return oldToken.address.equalsIgnoreCase(newToken.address) && oldToken.chainId == newToken.chainId;
        }
        else
        {
            return false;
        }
    }
}
