import sys
import yfinance as yf
import json
import datetime
from dateutil.relativedelta import relativedelta

def fetch_benchmark(tickers_json_str):
    try:
        portfolio_map = json.loads(tickers_json_str)
        end_date = datetime.date.today()
        start_date = end_date - relativedelta(days=30)
        
        tickers_to_fetch = list(portfolio_map.keys()) + ['^NSEI']
        data = yf.download(tickers_to_fetch, start=start_date, end=end_date, progress=False)['Close']
        
        dates = [d.strftime('%Y-%m-%d') for d in data.index]
        nifty = []
        portfolio = []
        
        for i in range(len(data)):
            if '^NSEI' in data.columns:
                val = data['^NSEI'].iloc[i]
                nifty.append(float(val) if val == val else 0)
            else:
                nifty.append(0)
            
            p_val = 0
            for t, q in portfolio_map.items():
                if t in data.columns:
                    v = data[t].iloc[i]
                    if v == v: # nan check
                        p_val += (float(v) * float(q))
            portfolio.append(p_val)
            
        print(json.dumps({
            "dates": dates,
            "nifty": nifty,
            "portfolio": portfolio
        }))
            
    except Exception as e:
        print("{}")

if __name__ == "__main__":
    import warnings
    warnings.simplefilter(action='ignore', category=FutureWarning)
    if len(sys.argv) > 1:
        fetch_benchmark(sys.argv[1])
    else:
        print("{}")
