import uuid
from django.db import models
from django.contrib.auth.models import AbstractBaseUser, BaseUserManager

from django.core.validators import MinLengthValidator, EmailValidator


class AccountManager(BaseUserManager):
    def create_user(self, email, password=None, **kwargs):
        if not email:
            raise ValueError("User must be have a valid Email Address")
        if not kwargs.get('username'):
            raise ValueError("User must have a valid Username")
        if not kwargs.get('first_name'):
            raise ValueError('User must have a valid First Name')
        if not kwargs.get('last_name'):
            raise ValueError('User must have a valid Last Name')

        account = self.model(
            email=self.normalize_email(email),
            username=kwargs.get('username'),
            first_name=kwargs.get('first_name'),
            last_name=kwargs.get('last_name'))

        account.set_password(password)
        account.save()

        return account


class Account(AbstractBaseUser):
    email = models.EmailField(unique=True, blank=False, validators=[EmailValidator])
    username = models.CharField(max_length=40, unique=True, blank=False, validators=[MinLengthValidator(2)])

    first_name = models.CharField(max_length=40, validators=[MinLengthValidator(2)])
    last_name = models.CharField(max_length=40, validators=[MinLengthValidator(2)])

    citizen_card = models.FileField(upload_to="media/citizen_pub_certs")
    has_cc = models.BooleanField(default=False)
    citizen_card_serial_number = models.CharField(unique=True, null=True, blank=True, max_length=256)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    objects = AccountManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['username', 'first_name', 'last_name']

    def __unicode__(self):
        return self.email

    def get_full_name(self):
        return ' '.join([self.first_name, self.last_name])

    def get_short_name(self):
        return self.first_name

    def has_citizen_card(self):
        return self.citizen_card is not None


class TokenForCitizenAuthentication(models.Model):
    identifier = models.CharField(max_length=100, blank=False, unique=True, default=uuid.uuid4)

