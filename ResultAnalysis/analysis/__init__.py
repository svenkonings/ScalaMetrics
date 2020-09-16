import argparse
import os

import pandas as pd
from sklearn.metrics import confusion_matrix, r2_score, precision_score, recall_score, matthews_corrcoef

categories = [
    'methodResultsBriand',
    'methodResultsLandkroon',
    'objectAvrResultsBriand',
    'objectSumResultsBriand',
    'objectMaxResultsBriand',
    'objectAvrResultsLandkroon',
    'objectSumResultsLandkroon',
    'objectMaxResultsLandkroon',
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


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    parser.add_argument('--exclude-columns', help="Select metrics to exclude from analysis", dest='exclude_columns',
                        nargs='+', default=list())
    parser.add_argument('--split-paradigm-score', help='Split analysis by paradigm score', dest='split_paradigm_score',
                        action="store_true")
    return parser.parse_args()


def get_columns(df, args):
    columns = list(df.select_dtypes(include='number').keys())
    columns.remove('faults')
    for column in args.exclude_columns:
        columns.remove(column)
    return columns


def to_binary(x):
    if x == 0:
        return 0
    else:
        return 1


def split_paradigm_score(df):
    neutral = df[df['HasPointsFraction'] == 0]
    df = df[df['HasPointsFraction'] != 0]
    oop = df[df['ParadigmScoreFraction'] <= -0.9]
    fp = df[df['ParadigmScoreFraction'] >= 0.9]
    mix = df.query('ParadigmScoreFraction > -0.9 & ParadigmScoreFraction < 0.9')
    return {
        'Neutral': neutral,
        'OOP': oop,
        'FP': fp,
        'Mix': mix
    }.items()


def get_stats(actual, predicted):
    tn, fp, fn, tp = confusion_matrix(actual, predicted).ravel()
    r2 = r2_score(actual, predicted)
    precision = precision_score(actual, predicted, zero_division=0) * 100
    recall = recall_score(actual, predicted, zero_division=0) * 100
    mcc = matthews_corrcoef(actual, predicted)
    return {
        'tn': tn,
        'fp': fp,
        'fn': fn,
        'tp': tp,
        'r2': r2,
        'precision': precision,
        'recall': recall,
        'mcc': mcc,
    }


def get_metric_results(folder, project, file):
    try:
        return pd.read_csv(f'../data/metricResults/{folder}/{project}/{file}.csv')
    except FileNotFoundError:
        return None


def save_dataframe(df, directory, filename, save_index=True):
    directory = f'../data/analysisResults/{directory}'
    os.makedirs(directory, exist_ok=True)
    df.to_csv(f'{directory}/{filename}.csv', index=save_index)
