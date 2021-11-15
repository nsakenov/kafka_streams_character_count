FROM tiangolo/uvicorn-gunicorn-fastapi:python3.7

WORKDIR /usr/app

ADD . /usr/app
RUN pip install -r requirements.txt
RUN chmod +x wait-for-it.sh
