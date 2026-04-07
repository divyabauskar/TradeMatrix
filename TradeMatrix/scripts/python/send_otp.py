import sys

def send_sms(mobile, otp):
    # Dummy mock response since user didn't config Twilio/Fast2SMS
    # We output to stdout so Java process can read it.
    print(f"MOCK SMS ENABLED. OTP for {mobile} is {otp}")
    sys.exit(0)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python send_otp.py <mobile> <otp>")
        sys.exit(1)
    
    mobile = sys.argv[1]
    otp = sys.argv[2]
    send_sms(mobile, otp)
