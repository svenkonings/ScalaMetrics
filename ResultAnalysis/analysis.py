import argparse
import os

import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import matthews_corrcoef, confusion_matrix, precision_score, recall_score, r2_score
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from main import projects


def main():
    for path, name in projects.items():
        function_regression(path, name)
        object_regression(path, name)
    save_global_statistics()


def function_regression(path, name):
    path = f'../target/{path}/functionResultsBriand.csv'
    df = pd.read_csv(path)
    add_fault_statistics(df, name, True)
    name = name + ' functions'
    regression(df, name)


def object_regression(path, name):
    path = f'../target/{path}/objectResultsBriand.csv'
    df = pd.read_csv(path)
    add_fault_statistics(df, name, False)
    name = name + ' objects'
    regression(df, name)


def regression(df, name):
    print(name)
    columns = get_columns(df)
    save_descriptive_statistics(df[columns], name)
    df['faulty'] = df['faults'].apply(to_binary)
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    multivariate_regression(df, estimator, cv, columns, name)
    univariate_regression(df, estimator, cv, columns, name)


def multivariate_regression(df, estimator, cv, columns, name):
    print(f'Multivariate {name}')
    faults = df['faulty']
    data = df[columns]
    prediction = cross_val_predict(estimator, data, faults, cv=cv)
    add_multivariate_result(faults, prediction, name)


def univariate_regression(df, estimator, cv, columns, name):
    print(f'Univariate {name}')
    faults = df['faulty']
    result = pd.DataFrame(columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc'])
    for column in columns:
        print(f'{name}: {column}')
        data = df[column].values.reshape(-1, 1)
        prediction = cross_val_predict(estimator, data, faults, cv=cv)
        column_result = get_stats(faults, prediction)
        column_result['name'] = column
        result = result.append(column_result, ignore_index=True)
    save_dataframe(result, 'results/univariate/', name, False)


def get_stats(actual, predicted):
    tn, fp, fn, tp = confusion_matrix(actual, predicted).ravel()
    r2 = r2_score(actual, predicted)
    precision = precision_score(actual, predicted)
    recall = recall_score(actual, predicted)
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


def get_columns(df):
    columns = list(df.select_dtypes(include='number').keys())
    columns.remove('faults')
    return columns


def to_binary(x):
    if x == 0:
        return 0
    else:
        return 1


def save_descriptive_statistics(df, name):
    statistics = df.describe().T.rename_axis('name', axis=0)
    save_dataframe(statistics, 'results/descriptive/', name)


def save_dataframe(df, directory, filename, save_index=True):
    os.makedirs(directory, exist_ok=True)
    df.to_csv(directory + filename + '.csv', index=save_index)


multivariate_regression_results = pd.DataFrame(
    columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
)

function_fault_statistics = pd.DataFrame(
    columns=['name', 'rows', 'faulty_rows', 'non_faulty_rows', 'percentage_faulty']
)
object_fault_statistics = pd.DataFrame(
    columns=['name', 'rows', 'faulty_rows', 'non_faulty_rows', 'percentage_faulty']
)


def add_multivariate_result(faults, prediction, name):
    result = get_stats(faults, prediction)
    result['name'] = name
    global multivariate_regression_results
    multivariate_regression_results = multivariate_regression_results.append(result, ignore_index=True)


def add_fault_statistics(df, name, is_function):
    total_rows = len(df)
    faulty_rows = len(df[df['faults'] > 0])
    non_faulty_rows = len(df[df['faults'] == 0])
    percentage_faulty = round((faulty_rows / total_rows) * 100, 2)
    result = {
        'name': name,
        'rows': total_rows,
        'faulty_rows': faulty_rows,
        'non_faulty_rows': non_faulty_rows,
        'percentage_faulty': percentage_faulty
    }
    if is_function:
        global function_fault_statistics
        function_fault_statistics = function_fault_statistics.append(result, ignore_index=True)
    else:
        global object_fault_statistics
        object_fault_statistics = object_fault_statistics.append(result, ignore_index=True)


def save_global_statistics():
    save_dataframe(multivariate_regression_results, 'results/', 'multivariateRegressionResults', False)
    save_dataframe(function_fault_statistics, 'results/', 'functionFaultStatistics', False)
    save_dataframe(object_fault_statistics, 'results/', 'objectFaultStatistics', False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    # parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    args = parser.parse_args()
    main()
