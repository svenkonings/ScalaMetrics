import warnings

import pandas as pd
from sklearn.exceptions import ConvergenceWarning
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, save_dataframe, get_metric_results, get_columns, to_binary, get_stats, \
    parse_args


def main(args):
    warnings.filterwarnings("ignore", category=RuntimeWarning, module="sklearn")
    warnings.filterwarnings("ignore", category=ConvergenceWarning, module="sklearn")
    folder = f'{args.folder}/regression/mulitvariate-baseline/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        for path, name in projects.items():
            baseline_df = get_metric_results('baseline', path, category)
            df = get_metric_results(args.folder, path, category)
            if df is None and 'object' in category:
                base_category = 'objectResultsBriand' if 'Briand' in category else 'objectResultsLandkroon'
                df = get_metric_results(args.folder, path, base_category)
            if baseline_df is not None and df is not None:
                multivariate_baseline(baseline_df, df, folder, category, name, estimator, cv, args)


def multivariate_baseline(baseline_df, df, folder, category, name, estimator, cv, args):
    print(f'[{category}] Mulitvariate baseline: {name}')
    faults = df['faults'].apply(to_binary)
    if len(faults[faults == 0]) < 10:
        print('Less than 10 non-faulty results -- skipping!')
        return
    if len(faults[faults == 1]) < 10:
        print('Less than 10 faulty results -- skipping!')
        return
    result = pd.DataFrame(
        columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
    )
    baseline_columns = get_columns(baseline_df, args)
    baseline_data = baseline_df[baseline_columns]
    print(f'[{category}] {name}: baseline')
    prediction = cross_val_predict(estimator, baseline_data, faults, cv=cv)
    baseline_result = get_stats(faults, prediction)
    baseline_result['name'] = 'baseline'
    result = result.append(baseline_result, ignore_index=True)
    columns = get_columns(df, args)
    for column in columns:
        print(f'[{category}] {name}: {column}')
        data = baseline_data.join(df[column])
        prediction = cross_val_predict(estimator, data, faults, cv=cv)
        column_result = get_stats(faults, prediction)
        column_result['name'] = column
        result = result.append(column_result, ignore_index=True)

    save_dataframe(result, folder + category, name, False)


if __name__ == '__main__':
    main(parse_args())
