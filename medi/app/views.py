from app import app
from flask import Flask, render_template, redirect, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_bootstrap import Bootstrap
from flask_admin import Admin, BaseView, expose
from flask_admin.form import rules
from flask_admin.contrib import sqla

import os
import os.path as op
import flask_admin as admin
import json

class Patient(BaseView):
  @expose('/')
  def index(self):
    return self.render('/admin/patients.html')


app = Flask(__name__)

app.config['SECRET_KEY'] = ['123456790']

@app.route('/')
def index():
  return '<a href="/admin">Return to Homepage</a>'

# create Admin
admin = Admin(app, name='MediBoard', template_mode='bootstrap3')
admin.add_view(Patient(name='My Patients', menu_icon_type='glyph', menu_icon_value='glyphicon-user'))




app.run(debug=True)