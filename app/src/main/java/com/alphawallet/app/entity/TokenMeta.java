package com.alphawallet.app.entity;

import com.alphawallet.app.repository.EthereumNetworkBase;
import com.alphawallet.app.repository.EthereumNetworkRepository;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TokenMeta
{
    public final String address;
    public final int chainId;
    public final int weighting;
    public final String balanceHash;
    public boolean update;
    public final ContractType contractType;
    public final boolean isTerminated;
    public final boolean zeroBalance;
    public float balanceUpdateWeight;
    public long  lastBalanceUpdate;

    public TokenMeta(int chainId, String address, String name, ContractType contractType, String balance)
    {
        this.address = address;
        this.chainId = chainId;
        this.contractType = contractType;
        this.weighting = calculateWeight(name);
        isTerminated = name == null || name.length() == 0;
        balanceHash = Numeric.toHexString(Hash.sha3(balance.getBytes()));
        if (balance.length() > 0 && balance.contains(",")) balance = convertBalance(balance);
        zeroBalance = balance == null || balance.length() == 0 || balance.equals("0");
        update = false;
        balanceUpdateWeight = calculateBalanceUpdateWeight(name);
        lastBalanceUpdate = System.currentTimeMillis();
    }

    public TokenMeta(Token token)
    {
        this.address = token.tokenInfo.address;
        this.chainId = token.tokenInfo.chainId;
        this.contractType = token.getInterfaceSpec();
        String name = token.getFullName();
        this.weighting = calculateWeight(name);
        isTerminated = name == null || name.length() == 0;
        balanceHash = Numeric.toHexString(Hash.sha3(token.getStringBalance().getBytes()));
        zeroBalance = !token.hasPositiveBalance();
        update = true;
        balanceUpdateWeight = calculateBalanceUpdateWeight(name);
        lastBalanceUpdate = System.currentTimeMillis();
    }

    public TokenMeta(TokenMeta tokenMeta, String balance)
    {
        this.address = tokenMeta.address;
        this.chainId = tokenMeta.chainId;
        this.contractType = tokenMeta.contractType;
        this.weighting = tokenMeta.weighting;
        isTerminated = tokenMeta.isTerminated;
        balanceHash = Numeric.toHexString(Hash.sha3(balance.getBytes()));
        if (balance.length() > 0 && balance.contains(",")) balance = convertBalance(balance);
        zeroBalance = balance == null || balance.length() == 0 || balance.equals("0");
        update = false;
        balanceUpdateWeight = tokenMeta.balanceUpdateWeight;
        lastBalanceUpdate = System.currentTimeMillis();
    }

    private int calculateWeight(String tokenName)
    {
        int weight = 1000; //ensure base eth types are always displayed first
        int override = EthereumNetworkRepository.getPriorityOverride(contractType, address, chainId);
        if (override > 0) return override;
        if(tokenName == null || tokenName.length() == 0) return Integer.MAX_VALUE;

        int i = 4;
        int pos = 0;

        while (i >= 0 && pos < tokenName.length())
        {
            char c = tokenName.charAt(pos++);
            //Character.isIdeographic()
            int w = tokeniseCharacter(c);
            if (w > 0)
            {
                int component = (int)Math.pow(26, i)*w;
                weight += component;
                i--;
            }
        }

        String address = Numeric.cleanHexPrefix(this.address);
        for (i = 0; i < address.length() && i < 2; i++)
        {
            char c = address.charAt(i);
            int w = c - '0';
            weight += w;
        }

        if (weight < 2) weight = 2;

        return weight;
    }

    private int tokeniseCharacter(char c)
    {
        int w = Character.toLowerCase(c) - 'a' + 1;
        if (w > 'z')
        {
            //could be ideographic, in which case we may want to display this first
            //just use a modulus
            w = w % 10;
        }
        else if (w < 0)
        {
            //must be a number
            w = 1 + (c - '0');
        }
        else
        {
            w += 10;
        }

        return w;
    }

    private String convertBalance(String balance)
    {
        String[] split = balance.split(",");
        for (String b : split)
        {
            BigInteger bi = new BigInteger(b, 16);
            if (bi.compareTo(BigInteger.ZERO) != 0) return "1";
        }

        return "0";
    }

    private float calculateBalanceUpdateWeight(String name)
    {
        float updateWeight = 0;
        //calculate balance update time
        if (!isTerminated)
        {
            if (EthereumNetworkBase.hasRealValue(chainId))
            {
                if (isEthereum()|| !zeroBalance)
                {
                    updateWeight = 1.0f;
                }
                else
                {
                    updateWeight = 0.5f;
                }
            }
            else
            {
                if (address.equalsIgnoreCase("0x3682663e6715d4fe500ea6425c429d41913b9c67"))
                {
                    updateWeight = 1.0f;
                }
                //testnet: TODO: check time since last transaction - if greater than 1 month slow update further
                else if (isEthereum())
                {
                    updateWeight = 0.25f;
                }
                else if (!zeroBalance)
                {
                    updateWeight = 0.1f;
                }
                else if (name == null || name.length() == 0)
                {
                    updateWeight = 0.005f;
                }
                else
                {
                    updateWeight = 0.05f;
                }
            }
        }

        //Log.d("TOKEN", tokenInfo.name + " Update weight " + updateWeight);

        return updateWeight;
    }

    public boolean isEthereum()
    {
        return contractType == ContractType.ETHEREUM;
    }

    public boolean isNFT()
    {
        return contractType == ContractType.ERC875 ||
                contractType == ContractType.ERC721 ||
                contractType == ContractType.ERC721_LEGACY ||
                contractType == ContractType.ERC875LEGACY;
    }

    public boolean hasIndepedentUpdate()
    {
        return  contractType == ContractType.ERC721 ||
                contractType == ContractType.ERC721_LEGACY;
    }
}
