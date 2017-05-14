from app import app
from flask import Flask, render_template, redirect, request, jsonify, url_for
from flask_sqlalchemy import SQLAlchemy
from flask_bootstrap import Bootstrap
from flask_admin import Admin, BaseView, expose
from flask_admin.form import rules
from flask_admin.contrib import sqla

import os
# import requests
import os.path as op
import flask_admin as admin
import json

class Patient(BaseView):
  @expose('/')
  def index(self):
    return self.render('/admin/patients.html')

class Logout(BaseView):
  @expose('/')
  def index(self):
    return self.render('/admin/logout.html')

app = Flask(__name__)

class AddPatient(BaseView):
  @expose('/')
  def index(self):
    return self.render('/admin/save-data.html')

@app.route('/')
def api_root():
    return 'Welcome'

@app.route('/articles')
def api_articles():
    return 'List of ' + url_for('api_articles')

@app.route('/articles/<articleid>')
def api_article(articleid):
    return 'You are reading ' + articleid

@app.route('/hello/')
def hello():
  if request.method == 'POST':
    return 'boxedId is SKS7-a4c1'
  else:
# create Admin
admin = Admin(app, name='MediBoard', template_mode='bootstrap3')
admin.add_view(Patient(name='My Patients', menu_icon_type='glyph', menu_icon_value='glyphicon-user'))
admin.add_view(Logout(name='Log Out', menu_icon_type='glyph', menu_icon_value='glyphicon-off'))
admin.add_view(AddPatient(name='Add Patient', menu_icon_type='glyph', menu_icon_value='glyphicon-user'))




app.run(debug=True)