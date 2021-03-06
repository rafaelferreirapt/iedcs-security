import base64
from Crypto import Random
from Crypto.Cipher import AES


class AESCipher(object):
    bs = 16  # 16 blocos * 8 bits = 128bits

    @staticmethod
    def encrypt(content, key, iv=None):
        if iv is None:
            iv = Random.new().read(AES.block_size)

        cipher = AES.new(key, AES.MODE_CBC, iv)
        return base64.b64encode(iv + cipher.encrypt(AESCipher.pad(content)))

    @staticmethod
    def decrypt(content_ciphered, key):
        enc = base64.b64decode(content_ciphered)
        cipher = AES.new(key, AES.MODE_CBC, enc[:AES.block_size])
        return AESCipher.unpad(cipher.decrypt(enc[AES.block_size:]))

    @staticmethod
    def pad(s):
        return s + (AESCipher.bs - len(s) % AESCipher.bs) * chr(AESCipher.bs - len(s) % AESCipher.bs)

    @staticmethod
    def unpad(s):
        return s[:-ord(s[len(s)-1:])]
