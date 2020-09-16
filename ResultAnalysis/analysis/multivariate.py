import warnings

import pandas as pd
from sklearn.exceptions import ConvergenceWarning
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, save_dataframe, get_metric_results, to_binary, get_columns, get_stats, \
    parse_args


def main(args):
    warnings.filterwarnings("ignore", category=ConvergenceWarning, module="sklearn")
    folder = f'{args.folder}/regression/multivariate/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        regression_results = pd.DataFrame(
            columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
        )
        for path, name in projects.items():
            df = get_metric_results(args.folder, path, category)
            if df is not None:
                regression_results = multivatiate(df, regression_results, category, name, estimator, cv, args)
        if not regression_results.empty:
            save_dataframe(regression_results, folder, category, False)


def multivatiate(df, regression_results, category, name, estimator, cv, args):
    print(f'[{category}] Multivariate: {name}')
    columns = get_columns(df, args)
    faults = df['faults'].apply(to_binary)
    data = df[columns]
    prediction = cross_val_predict(estimator, data, faults, cv=cv)
    result = get_stats(faults, prediction)
    result['name'] = name
    return regression_results.append(result, ignore_index=True)


if __name__ == '__main__':
    main(parse_args())
