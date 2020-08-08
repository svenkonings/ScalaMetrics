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
    columns = list(df.select_dtypes(include=['float64', 'int64', 'int', 'float']).keys())
    columns.remove('faults')
    return columns


def univariate_regression(df, name):
    print(name)
    faults = df['faults'].apply(faulty)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    columns = get_columns(df)
    print("Multivariate")
    regression = LogisticRegression(class_weight='balanced', random_state=42, multi_class='multinomial')
    prediction = cross_val_predict(regression, df[columns].values, faults, cv=cv)
    print_stats(faults, prediction)
    print('Univariate')
    for column in columns:
        regression = LogisticRegression(class_weight='balanced', random_state=42)
        prediction = cross_val_predict(regression, df[column].values.reshape(-1, 1), faults, cv=cv)
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
    univariate_regression(df, name)


def object_regression(path, name):
    path = f'../target/{path}/objectResultsBriand.csv'
    name = name + ' objects'
    df = pd.read_csv(path)
    univariate_regression(df, name)


def main():
    for path, name in projects.items():
        function_regression(path, name)
        object_regression(path, name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    # parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    args = parser.parse_args()
    main()
