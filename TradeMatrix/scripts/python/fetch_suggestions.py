import sys
import requests
import json

def get_suggestions(query):
    url = f"https://query2.finance.yahoo.com/v1/finance/search?q={query}&quotesCount=10&newsCount=0"
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
    try:
        res = requests.get(url, headers=headers, timeout=5)
        data = res.json()
        quotes = data.get('quotes', [])
        results = [q['symbol'] for q in quotes if 'symbol' in q]
        print(json.dumps(results))
    except Exception as e:
        print("[]")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        get_suggestions(sys.argv[1])
    else:
        print("[]")
