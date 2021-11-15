"""Produce fake transactions into a Kafka topic."""
import os
import json
from kafka import KafkaProducer, KafkaConsumer
import firebase_admin
import threading
from firebase_admin import firestore
import asyncio

# kafka producer
producer = KafkaProducer(
    bootstrap_servers=os.environ.get('KAFKA_BROKER_URL'),
    # Encode all values as JSON
    value_serializer=lambda value: json.dumps(value).encode(),
)

# Create a callback on_snapshot function to capture changes
def on_snapshot(doc_snapshot, changes, read_time):
    for doc in doc_snapshot:
        print(doc.to_dict())
        doc = doc.to_dict()
        producer.send(os.environ.get('INPUT_TOPIC'), value=doc['text'])
        callback_done.set()

# create an Event for notifying main thread.
callback_done = threading.Event()

# init Firebase
cred_object = firebase_admin.credentials.Certificate("firebase-sdk.json")
firebase_admin.initialize_app(cred_object)
firestore_db = firebase_admin.firestore.client()

async def main():
    # subscribe to firebase changes
    doc_ref = firestore_db.collection(u'character-count-input').document('input')
    doc_ref.on_snapshot(on_snapshot)
    await asyncio.Future() #run forever

asyncio.run(main())
