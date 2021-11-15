import os
import json
from kafka import KafkaConsumer
import time
import firebase_admin
import threading
from firebase_admin import firestore
import asyncio


OUTPUT_TOPIC = os.environ.get('OUTPUT_TOPIC')
KAFKA_BROKER_URL = os.environ.get('KAFKA_BROKER_URL')

consumer = KafkaConsumer(
    OUTPUT_TOPIC,
    bootstrap_servers=KAFKA_BROKER_URL,
    value_deserializer=lambda value: msg_process(value),
)

def msg_process(msg):
    time_start = time.strftime("%Y-%m-%d %H:%M:%S")
    message = msg.decode("utf-8")
    # message['time'] = time_start
    return message

# init Firebase
cred_object = firebase_admin.credentials.Certificate("firebase-sdk.json")
firebase_admin.initialize_app(cred_object)
firestore_db = firebase_admin.firestore.client()

#create & update

for message in consumer:
    result = message.value
    print(result)
    # doc_ref = firestore_db.collection(u'character-count-input').document(result['key'])
    # doc_ref.set(result)
    