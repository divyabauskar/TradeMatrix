import sys
import os
import requests
from dotenv import load_dotenv

def send_sms(mobile, otp):
    env_path = os.path.join(os.path.dirname(__file__), '.env')
    load_dotenv(env_path)
    api_key = os.getenv("FAST2SMS_API_KEY")
    
    # Preempt any missing +91 depending on Fast2SMS requirements
    if mobile.startswith("+91"):
        mobile = mobile[3:]
        
    if not api_key or api_key == "YOUR_FAST2SMS_KEY_HERE":
        print("ERROR: FAST2SMS API key missing in scripts/python/.env file!")
        print(f"MOCK SMS FALLBACK. OTP for {mobile} is {otp}")
        sys.exit(0)

    url = "https://www.fast2sms.com/dev/bulkV2"
    # Switching route from 'otp' to 'q' (Quick SMS) to bypass website verification
    payload = f"route=q&message=Your TradeMatrix OTP is {otp}&language=english&flash=0&numbers={mobile}"
    headers = {
        'authorization': api_key,
        'Content-Type': "application/x-www-form-urlencoded",
        'Cache-Control': "no-cache",
    }
    
    try:
        response = requests.post(url, data=payload, headers=headers)
        if response.status_code == 200:
            print("OTP Sent successfully via Fast2SMS API!")
        else:
            print(f"Failed to send SMS (API Error): {response.text}")
    except Exception as e:
        print(f"Failed to send SMS (Network Exception): {str(e)}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python engine.py <mobile> <otp>")
        sys.exit(1)
    
    mobile = sys.argv[1]
    otp = sys.argv[2]
    send_sms(mobile, otp)
