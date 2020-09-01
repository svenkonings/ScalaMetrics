import os

import pandas as pd
from sklearn.metrics import confusion_matrix, r2_score, precision_score, recall_score, matthews_corrcoef

categories = [
    'methodResultsBriand',
    'methodResultsLandkroon',
    'objectMethodAvrResultsBriand',
    'objectMethodSumResultsBriand',
    'objectMethodMaxResultsBriand',
    'objectMethodAvrResultsLandkroon',
    'objectMethodSumResultsLandkroon',
    'objectMethodMaxResultsLandkroon',
]

projects = {
    'akka': 'Akka',
    'gitbucket': 'Gitbucket',
    'http4s': 'Http4s',
    'quill': 'Quill',
    'scio': 'Scio',
    'shapeless': 'Shapeless',
    'zio': 'ZIO',
}


def get_columns(df):
    columns = list(df.select_dtypes(include='number').keys())
    columns.remove('faults')
    return columns


def to_binary(x):
    if x == 0:
        return 0
    else:
        return 1


def get_stats(actual, predicted):
    tn, fp, fn, tp = confusion_matrix(actual, predicted).ravel()
    r2 = r2_score(actual, predicted)
    precision = precision_score(actual, predicted) * 100
    recall = recall_score(actual, predicted) * 100
    mcc = matthews_corrcoef(actual, predicted)
    return {
        'tn': tn,
        'fp': fp,
        'fn': fn,
        'tp': tp,
        'r2': r2,
        'precision': precision,
        'recall': recall,
        'mcc': mcc
    }


def get_metric_results(folder, project, file):
    return pd.read_csv(f'../data/metricResults/{folder}/{project}/{file}.csv')


def save_dataframe(df, directory, filename, save_index=True):
    directory = f'../data/analysisResults/{directory}'
    os.makedirs(directory, exist_ok=True)
    df.to_csv(f'{directory}/{filename}.csv', index=save_index)
