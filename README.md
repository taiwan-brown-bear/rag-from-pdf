# rag-from-pdf

The following is the advantages of RAG:

1. cost efficiency: token is the currency of LLM. The more number of tokens in the request, the more expensive the call is ...
   by using similarity searching, only the more relevant info is sent along.

2. up to date response: LLM has cut off date.
   by providing more update to date info, the more up to date answer can be found by LLM.

3. privacy concern: You don't want LLM to be trained with privacy data.
   However, you might want LLM to provide an more personalized response.

Troubleshootings:
