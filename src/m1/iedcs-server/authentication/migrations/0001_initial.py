# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import django.utils.timezone
import django.core.validators


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Account',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('password', models.CharField(max_length=128, verbose_name='password')),
                ('last_login', models.DateTimeField(default=django.utils.timezone.now, verbose_name='last login')),
                ('email', models.EmailField(unique=True, max_length=75, validators=[django.core.validators.EmailValidator])),
                ('username', models.CharField(unique=True, max_length=40, validators=[django.core.validators.MinLengthValidator(2)])),
                ('first_name', models.CharField(max_length=40, validators=[django.core.validators.MinLengthValidator(2)])),
                ('last_name', models.CharField(max_length=40, validators=[django.core.validators.MinLengthValidator(2)])),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
            ],
            options={
                'abstract': False,
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='UserCollectedData',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('cpu_model', models.CharField(default=b'', max_length=128)),
                ('op_system', models.CharField(default=b'', max_length=128)),
                ('ip', models.CharField(default=b'', max_length=128)),
                ('country', models.CharField(default=b'', max_length=128)),
                ('timezone', models.CharField(default=b'', max_length=128)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.AddField(
            model_name='account',
            name='user_data',
            field=models.ForeignKey(to='authentication.UserCollectedData', blank=True),
            preserve_default=True,
        ),
    ]
