import argparse

import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import matthews_corrcoef, confusion_matrix, precision_score, recall_score
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from main import projects


def faulty(faults):
    if faults == 0:
        return 0
    else:
        return 1


def get_columns(df):
    columns = list(df.select_dtypes(include='number').keys())
    columns.remove('faults')
    return columns


def regression(df, name):
    print(name)
    columns = get_columns(df)
    df['faulty'] = df['faults'].apply(faulty)
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    multivariate_regression(df, estimator, cv, columns)
    univariate_regression(df, estimator, cv, columns)


def multivariate_regression(df, estimator, cv, columns):
    print("Multivariate")
    faults = df['faulty']
    data = df[columns]
    prediction = cross_val_predict(estimator, data, faults, cv=cv)
    print_stats(faults, prediction)


def univariate_regression(df, estimator, cv, columns):
    print('Univariate')
    faults = df['faulty']
    for column in columns:
        data = df[column].values.reshape(-1, 1)
        prediction = cross_val_predict(estimator, data, faults, cv=cv)
        print(column)
        print_stats(faults, prediction)


def print_stats(actual, predicted):
    cm = confusion_matrix(actual, predicted)
    print(cm)
    p = precision_score(actual, predicted)
    print(f'Precision: {p}')
    r = recall_score(actual, predicted)
    print(f'Recall: {r}')
    mcc = matthews_corrcoef(actual, predicted)
    print(f'MCC: {mcc}')


def function_regression(path, name):
    path = f'../target/{path}/functionResultsBriand.csv'
    name = name + ' functions'
    df = pd.read_csv(path)
    regression(df, name)


def object_regression(path, name):
    path = f'../target/{path}/objectResultsBriand.csv'
    name = name + ' objects'
    df = pd.read_csv(path)
    regression(df, name)


def main():
    for path, name in projects.items():
        function_regression(path, name)
        object_regression(path, name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    # parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    args = parser.parse_args()
    main()
